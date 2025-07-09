package com.devstaq.auth.profile.session;

import com.devstaq.auth.persistence.model.User;
import com.devstaq.auth.profile.BaseUserProfile;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base class for session-scoped user profile management. This class provides the foundation for maintaining user profile data within the session
 * context of a web application. It is designed to be extended by applications to add custom profile management functionality.
 *
 * <p>
 * This class is session-scoped and uses proxy mode TARGET_CLASS to ensure proper session management in a web environment. It maintains a reference to
 * the user's profile and tracks when it was last updated.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code
 * @Component
 * public class CustomSessionProfile extends BaseSessionProfile<CustomUserProfile> {
 *     // Add custom methods for your application
 *     public boolean hasSpecificPermission() {
 *         return getUserProfile().getPermissions().contains("SPECIFIC_PERMISSION");
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of user profile, must extend BaseUserProfile
 *
 * @see BaseUserProfile
 * @see WebApplicationContext
 * @see Serializable
 */
@Data
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public abstract class BaseSessionProfile<T extends BaseUserProfile> implements Serializable {

    /** Serialization version ID. */
    private static final long serialVersionUID = 1L;

    /** The current user's profile. */
    private T userProfile;

    /** Timestamp of when the profile was last updated. */
    private LocalDateTime lastUpdated;

    /**
     * Retrieves the current user's profile.
     *
     * @return the user profile of type T, or null if no profile is set
     */
    public T getUserProfile() {
        return userProfile;
    }

    /**
     * Sets the user's profile and updates the lastUpdated timestamp. This method is typically called during authentication or when the profile data
     * is modified.
     *
     * @param userProfile the user profile to set
     */
    public void setUserProfile(T userProfile) {
        this.userProfile = userProfile;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Convenience method to get the core User entity associated with the profile.
     *
     * @return the User entity if a profile is set, null otherwise
     * @see User
     */
    public User getUser() {
        return userProfile != null ? userProfile.getUser() : null;
    }
}
