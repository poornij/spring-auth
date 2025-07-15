package com.devstaq.auth.listener;

import com.devstaq.auth.audit.AuditEvent;
import com.devstaq.auth.event.OnRegistrationCompleteEvent;
import com.devstaq.auth.persistence.model.User;
import com.devstaq.auth.util.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListenerUtils {

    private final ApplicationEventPublisher eventPublisher;
    /**
     * Publishes a registration event.
     *
     * @param user the registered user
     * @param request the HTTP servlet request
     */
    public void publishRegistrationEvent(User user, HttpServletRequest request) {
        String appUrl = request.getContextPath();
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl));
    }

    /**
     * Logs an audit event.
     *
     * @param action the action performed
     * @param status the status of the action
     * @param message the message describing the action
     * @param user the user involved in the action
     * @param request the HTTP servlet request
     */
    public void logAuditEvent(String action, String status, String message, User user, HttpServletRequest request) {
        AuditEvent event =
                AuditEvent.builder().source(this).user(user).sessionId(request.getSession().getId()).ipAddress(UserUtils.getClientIP(request))
                        .userAgent(request.getHeader("User-Agent")).action(action).actionStatus(status).message(message).build();
        eventPublisher.publishEvent(event);
    }
}
