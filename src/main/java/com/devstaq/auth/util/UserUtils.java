package com.devstaq.auth.util;

import com.devstaq.auth.audit.AuditEvent;
import com.devstaq.auth.dto.UserDto;
import com.devstaq.auth.event.OnRegistrationCompleteEvent;
import com.devstaq.auth.persistence.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for user-related operations.
 */
public final class UserUtils {
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private UserUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Get the client's IP address.
	 *
	 * @param request The HttpServletRequest object.
	 * @return The client's IP address as a String.
	 */
	public static String getClientIP(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader != null) {
			return xfHeader.split(",")[0];
		}
		return request.getRemoteAddr();
	}

	/**
	 * Get the application URL based on the provided request.
	 *
	 * @param request The HttpServletRequest object.
	 * @return The application URL as a String.
	 */
	public static String getAppUrl(HttpServletRequest request) {
		return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
	}

	/**
	 * Validates the user data transfer object.
	 *
	 * @param userDto the user data transfer object
	 */
	public static void validateUserDto(UserDto userDto) {
		if (isNullOrEmpty(userDto.getEmail())) {
			throw new IllegalArgumentException("Email is required.");
		}
		if (isNullOrEmpty(userDto.getPassword())) {
			throw new IllegalArgumentException("Password is required.");
		}
	}

	/**
	 * Checks if a string is null or empty.
	 *
	 * @param value
	 * @return true if the string is null or empty, false otherwise
	 */
	public static boolean isNullOrEmpty(String value) {
		return value == null || value.isEmpty();
	}

	/**
	 * Builds an error response.
	 *
	 * @param message
	 * @param code
	 * @param status
	 * @return a ResponseEntity containing a JSONResponse with the error response
	 */
	public static ResponseEntity<JSONResponse> buildErrorResponse(String message, int code, HttpStatus status) {
		return ResponseEntity.status(status).body(JSONResponse.builder().success(false).code(code).message(message).build());
	}

	/**
	 * Builds a success response.
	 *
	 * @param message
	 * @param redirectUrl
	 * @return a ResponseEntity containing a JSONResponse with the success response
	 */
	public static ResponseEntity<JSONResponse> buildSuccessResponse(String message, String redirectUrl) {
		return ResponseEntity.ok(JSONResponse.builder().success(true).code(0).message(message).redirectUrl(redirectUrl).build());
	}
}
