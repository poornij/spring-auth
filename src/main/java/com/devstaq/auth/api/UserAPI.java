package com.devstaq.auth.api;

import com.devstaq.auth.dto.PasswordDto;
import com.devstaq.auth.dto.UserDto;
import com.devstaq.auth.exceptions.InvalidOldPasswordException;
import com.devstaq.auth.listener.ListenerUtils;
import com.devstaq.auth.persistence.model.User;
import com.devstaq.auth.service.DSUserDetails;
import com.devstaq.auth.service.UserService;
import com.devstaq.auth.util.JSONResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

import static com.devstaq.auth.util.UserUtils.buildErrorResponse;
import static com.devstaq.auth.util.UserUtils.buildSuccessResponse;

/**
 * REST controller for managing user-related operations. This class handles user registration, account deletion, and other user-related endpoints.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/user", produces = "application/json")
public class UserAPI {

	private final UserService userService;
	private final MessageSource messages;
	private final ListenerUtils listenerUtils;

	@Value("${user.security.registrationPendingURI}")
	private String registrationPendingURI;

	@Value("${user.security.registrationSuccessURI}")
	private String registrationSuccessURI;

	@Value("${user.security.forgotPasswordPendingURI}")
	private String forgotPasswordPendingURI;

	/**
	 * Updates the user's password. This is used when the user is logged in and wants to change their password.
	 *
	 * @param userDetails the authenticated user details
	 * @param userDto the user data transfer object containing user details
	 * @param request the HTTP servlet request
	 * @param locale the locale
	 * @return a ResponseEntity containing a JSONResponse with the password update result
	 */
	@PostMapping("/updateUser")
	public ResponseEntity<JSONResponse> updateUserAccount(@AuthenticationPrincipal DSUserDetails userDetails, @Valid @RequestBody UserDto userDto,
			HttpServletRequest request, Locale locale) {
		validateAuthenticatedUser(userDetails);
		User user = userDetails.getUser();
		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		userService.saveRegisteredUser(user);

		listenerUtils.logAuditEvent("ProfileUpdate", "Success", "User profile updated", user, request);

		return buildSuccessResponse(messages.getMessage("message.update-user.success", null, locale), null);
	}

	/**
	 * Updates the user's password. This is used when the user is logged in and wants to change their password.
	 *
	 * @param userDetails the authenticated user details
	 * @param passwordDto the password data transfer object containing the old and new passwords
	 * @param request the HTTP servlet request
	 * @param locale the locale
	 * @return a ResponseEntity containing a JSONResponse with the password update result
	 */
	@PostMapping("/updatePassword")
	public ResponseEntity<JSONResponse> updatePassword(@AuthenticationPrincipal DSUserDetails userDetails,
			@Valid @RequestBody PasswordDto passwordDto, HttpServletRequest request, Locale locale) {
		validateAuthenticatedUser(userDetails);
		User user = userDetails.getUser();

		try {
			if (!userService.checkIfValidOldPassword(user, passwordDto.getOldPassword())) {
				throw new InvalidOldPasswordException("Invalid old password");
			}

			userService.changeUserPassword(user, passwordDto.getNewPassword());
			listenerUtils.logAuditEvent("PasswordUpdate", "Success", "User password updated", user, request);

			return buildSuccessResponse(messages.getMessage("message.update-password.success", null, locale), null);
		} catch (InvalidOldPasswordException ex) {
			listenerUtils.logAuditEvent("PasswordUpdate", "Failure", "Invalid old password", user, request);
			return buildErrorResponse(messages.getMessage("message.update-password.invalid-old", null, locale), 1, HttpStatus.BAD_REQUEST);
		} catch (Exception ex) {
			log.error("Unexpected error during password update.", ex);
			listenerUtils.logAuditEvent("PasswordUpdate", "Failure", ex.getMessage(), user, request);
			return buildErrorResponse("System Error!", 5, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Deletes the user's account. This is used when the user wants to delete their account. This will either delete the account or disable it based
	 * on the configuration of the actuallyDeleteAccount property. After the account is disabled or deleted, the user will be logged out.
	 *
	 * @param userDetails the authenticated user details
	 * @param request the HTTP servlet request
	 * @return a ResponseEntity containing a JSONResponse with the account deletion result
	 */
	@DeleteMapping("/deleteAccount")
	public ResponseEntity<JSONResponse> deleteAccount(@AuthenticationPrincipal DSUserDetails userDetails, HttpServletRequest request) {
		validateAuthenticatedUser(userDetails);
		User user = userDetails.getUser();
		userService.deleteOrDisableUser(user);
		listenerUtils.logAuditEvent("AccountDelete", "Success", "User account deleted", user, request);
		logoutUser(request);
		return buildSuccessResponse("Account Deleted", null);
	}

	// Helper Methods

	/**
	 * Validates the authenticated user.
	 *
	 * @param userDetails the authenticated user details
	 */
	private void validateAuthenticatedUser(DSUserDetails userDetails) {
		if (userDetails == null || userDetails.getUser() == null) {
			throw new SecurityException("User not logged in.");
		}
	}

	/**
	 * Logs out the user.
	 *
	 * @param request the HTTP servlet request
	 */
	private void logoutUser(HttpServletRequest request) {
		try {
			SecurityContextHolder.clearContext();
			request.logout();
		} catch (ServletException e) {
			log.warn("Logout failed during account deletion.", e);
		}
	}

}
