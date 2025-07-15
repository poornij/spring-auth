package com.devstaq.auth.api;

import com.devstaq.auth.audit.AuditEvent;
import com.devstaq.auth.dto.UserDto;
import com.devstaq.auth.exceptions.UserAlreadyExistException;
import com.devstaq.auth.listener.ListenerUtils;
import com.devstaq.auth.persistence.model.User;
import com.devstaq.auth.service.UserEmailService;
import com.devstaq.auth.service.UserService;
import com.devstaq.auth.service.UserVerificationService;
import com.devstaq.auth.util.JSONResponse;
import com.devstaq.auth.util.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.devstaq.auth.util.UserUtils.*;

/**
 * REST Controller for user-related actions, providing pure REST endpoints
 * without MVC view rendering.
 */
@Slf4j
@RestController
@RequestMapping("/api/user") // Base path for all user-related REST endpoints
@RequiredArgsConstructor
public class AuthController {
    private static final String AUTH_MESSAGE_PREFIX = "auth.message.";

    private final UserService userService;
    private final UserVerificationService userVerificationService;
    private final MessageSource messages;
    private final ApplicationEventPublisher eventPublisher;
    private final ListenerUtils listenerUtils;
    private final UserEmailService userEmailService;

    // URIs configured in application.properties - these are now for internal reference
    // or for clients to know where to redirect, not for server-side redirection.
    @Value("${user.security.registrationPendingURI}")
    private String registrationPendingURI;

    @Value("${user.security.registrationSuccessURI}")
    private String registrationSuccessURI;

    @Value("${user.security.registrationNewVerificationURI}")
    private String registrationNewVerificationURI;

    @Value("${user.security.forgotPasswordPendingURI}")
    private String forgotPasswordPendingURI;

    @Value("${user.security.forgotPasswordChangeURI}")
    private String forgotPasswordChangeURI;

    /**
     * Registers a new user account.
     *
     * @param userDto the user data transfer object containing user details
     * @param request the HTTP servlet request
     * @return a ResponseEntity containing a JSONResponse with the registration result
     */
    @PostMapping("/registration")
    public ResponseEntity<JSONResponse> registerUserAccount(@Valid @RequestBody UserDto userDto, HttpServletRequest request) {
        try {
            validateUserDto(userDto);
            User registeredUser = userService.registerNewUserAccount(userDto);
            listenerUtils.publishRegistrationEvent(registeredUser, request);
            listenerUtils.logAuditEvent("Registration", "Success", "Registration Successful", registeredUser, request);

            String nextURL = registeredUser.isEnabled() ? handleAutoLogin(registeredUser) : registrationPendingURI;

            return buildSuccessResponse("Registration Successful!", nextURL);
        } catch (UserAlreadyExistException ex) {
            log.warn("User already exists with email: {}", userDto.getEmail());
            listenerUtils.logAuditEvent("Registration", "Failure", "User Already Exists", null, request);
            return buildErrorResponse("An account already exists for the email address", 2, HttpStatus.CONFLICT);
        } catch (Exception ex) {
            log.error("Unexpected error during registration.", ex);
            listenerUtils.logAuditEvent("Registration", "Failure", ex.getMessage(), null, request);
            return buildErrorResponse("System Error!", 5, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Resends the registration token. This is used when the user did not receive the initial registration email.
     *
     * @param userDto the user data transfer object containing user details
     * @param request the HTTP servlet request
     * @return a ResponseEntity containing a JSONResponse with the registration result
     */
    @PostMapping("/resendRegistrationToken")
    public ResponseEntity<JSONResponse> resendRegistrationToken(@Valid @RequestBody UserDto userDto, HttpServletRequest request) {
        User user = userService.findUserByEmail(userDto.getEmail());
        if (user != null) {
            if (user.isEnabled()) {
                return buildErrorResponse("Account is already verified.", 1, HttpStatus.CONFLICT);
            }
            userEmailService.sendRegistrationVerificationEmail(user, UserUtils.getAppUrl(request));
            listenerUtils.logAuditEvent("Resend Reg Token", "Success", "Verification Email Resent", user, request);
            return buildSuccessResponse("Verification Email Resent Successfully!", registrationPendingURI);
        }
        return buildErrorResponse("System Error!", 2, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * This is used when the user has forgotten their password and wants to reset their password. This will send an email to the user with a link to
     * reset their password.
     *
     * @param userDto the user data transfer object containing user details
     * @param request the HTTP servlet request
     * @return a ResponseEntity containing a JSONResponse with the password reset email send result
     */
    @PostMapping("/resetPassword")
    public ResponseEntity<JSONResponse> resetPassword(@Valid @RequestBody UserDto userDto, HttpServletRequest request) {
        User user = userService.findUserByEmail(userDto.getEmail());
        if (user != null) {
            userEmailService.sendForgotPasswordVerificationEmail(user, UserUtils.getAppUrl(request));
            listenerUtils.logAuditEvent("Reset Password", "Success", "Password reset email sent", user, request);
        }
        return buildSuccessResponse("If account exists, password reset email has been sent!", forgotPasswordPendingURI);
    }

    /**
     * Validates a forgot password token. If valid, returns a success response.
     * Otherwise, returns an error response with the reason for failure.
     *
     * @param request The HttpServletRequest to extract client details.
     * @param token   The password reset token.
     * @return A ResponseEntity containing a map with status and message.
     */
    @GetMapping("/changePassword") // Changed path to be relative to /api/user
    public ResponseEntity<Map<String, String>> validateChangePasswordToken(
            final HttpServletRequest request,
            @RequestParam("token") final String token) {

        log.debug("UserActionRestController.validateChangePasswordToken: called with token: {}", token);
        final UserService.TokenValidationResult result = userService.validatePasswordResetToken(token);
        log.debug("UserActionRestController.validateChangePasswordToken: result: {}", result);

        // Build and publish audit event
        AuditEvent changePasswordAuditEvent = AuditEvent.builder()
                .source(this)
                .sessionId(request.getSession().getId())
                .ipAddress(UserUtils.getClientIP(request))
                .userAgent(request.getHeader("User-Agent"))
                .action("validateChangePasswordToken")
                .actionStatus(UserService.TokenValidationResult.VALID.equals(result) ? "Success" : "Failure")
                .message("Token validation result: " + result)
                .build();
        eventPublisher.publishEvent(changePasswordAuditEvent);

        Map<String, String> response = new HashMap<>();
        if (UserService.TokenValidationResult.VALID.equals(result)) {
            response.put("status", "success");
            response.put("message", "Token is valid. Proceed to change password.");
            response.put("token", token); // Optionally return the token for client-side use
            response.put("redirectUrl", forgotPasswordChangeURI); // Suggest client redirect
            return ResponseEntity.ok(response);
        } else {
            String messageKey = AUTH_MESSAGE_PREFIX + result.name().toLowerCase();
            String errorMessage = messages.getMessage(messageKey, null, Locale.getDefault()); // Use default locale for REST error
            response.put("status", "error");
            response.put("message", errorMessage);
            response.put("errorCode", result.name());
            return ResponseEntity.badRequest().body(response); // 400 Bad Request
        }
    }

    /**
     * Validates a registration verification token. If valid, confirms the user's registration.
     * Returns a success response or an error response.
     *
     * @param request The HttpServletRequest to extract client details and locale.
     * @param token   The verification token.
     * @return A ResponseEntity containing a map with status and message.
     * @throws UnsupportedEncodingException If there's an encoding issue (though less likely for token validation).
     */
    @GetMapping("/registrationConfirm") // Changed path to be relative to /api/user
    public ResponseEntity<Map<String, String>> confirmRegistration(
            final HttpServletRequest request,
            @RequestParam("token") final String token) throws UnsupportedEncodingException {

        log.debug("UserActionRestController.confirmRegistration: called with token: {}", token);
        Locale locale = request.getLocale(); // Still useful for message sourcing
        final UserService.TokenValidationResult result = userVerificationService.validateVerificationToken(token);

        Map<String, String> response = new HashMap<>();
        if (result == UserService.TokenValidationResult.VALID) {
            final User user = userVerificationService.getUserByVerificationToken(token);
            if (user != null) {
                userService.authWithoutPassword(user); // This might need review in a pure REST context
                // as it implies server-side session management.
                // For pure REST, client would typically log in after confirmation.
                // Keeping it as per original for direct translation.
                userVerificationService.deleteVerificationToken(token);

                // Build and publish audit event
                AuditEvent registrationAuditEvent = AuditEvent.builder()
                        .source(this)
                        .user(user)
                        .sessionId(request.getSession().getId())
                        .ipAddress(UserUtils.getClientIP(request))
                        .userAgent(request.getHeader("User-Agent"))
                        .action("Registration Confirmation")
                        .actionStatus("Success")
                        .message("Registration Confirmed. User logged in (server-side).")
                        .build();
                eventPublisher.publishEvent(registrationAuditEvent);

                response.put("status", "success");
                response.put("message", messages.getMessage("message.account.verified", null, locale));
                response.put("redirectUrl", registrationSuccessURI); // Suggest client redirect
                log.debug("UserActionRestController.confirmRegistration: account verified!");
                return ResponseEntity.ok(response);
            } else {
                // User not found for a valid token (should ideally not happen if token is valid)
                response.put("status", "error");
                response.put("message", "User not found for the provided token.");
                response.put("errorCode", "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 Not Found
            }
        } else {
            String messageKey = AUTH_MESSAGE_PREFIX + result.name().toLowerCase();
            String errorMessage = messages.getMessage(messageKey, null, locale);
            response.put("status", "error");
            response.put("message", errorMessage);
            response.put("errorCode", result.name());
            response.put("expired", String.valueOf(result == UserService.TokenValidationResult.EXPIRED));
            response.put("token", token); // Optionally return token for client to resend
            response.put("redirectUrl", registrationNewVerificationURI); // Suggest client redirect
            log.debug("UserActionRestController.confirmRegistration: failed. Token not found or expired.");
            return ResponseEntity.badRequest().body(response); // 400 Bad Request
        }
    }

    /**
     * Handles the auto login of the user after registration.
     *
     * @param user the registered user
     * @return the URI to redirect to after registration
     */
    private String handleAutoLogin(User user) {
        userService.authWithoutPassword(user);
        return registrationSuccessURI;
    }

}
