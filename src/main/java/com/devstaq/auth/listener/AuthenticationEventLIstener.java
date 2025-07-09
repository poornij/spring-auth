package com.devstaq.auth.listener;

import com.devstaq.auth.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * This class is used to listen for authentication events and handle account lockout functionality if needed.
 *
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationEventLIstener {

    final private LoginAttemptService loginAttemptService;

    /**
     * This method listens for successful authentications and handles account lockout functionality.
     *
     * @param success the success event
     */
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        // Handle successful authentication, e.g. logging or auditing
        log.debug("Authentication success: " + success.getAuthentication().getName());
        String username = success.getAuthentication().getName();
        loginAttemptService.loginSucceeded(username);
    }

    /**
     * This method listens for authentication failures and handles account lockout functionality.
     *
     * @param failure the failure event
     */
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failure) {
        // Handle unsuccessful authentication, e.g. logging or auditing
        log.debug("Authentication failure: " + failure.getException().getMessage());
        String username = failure.getAuthentication().getName();
        loginAttemptService.loginFailed(username);
    }

}
