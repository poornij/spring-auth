package com.devstaq.auth.service;

import com.devstaq.auth.persistence.model.User;
import com.devstaq.auth.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private UserRepository userRepository;

    private LoginAttemptService loginAttemptService;

    private final int failedLoginAttempts = 10; // Assuming these are the values in your application.properties
    private final int accountLockoutDuration = 1; // Assuming these are the values in your application.properties

    private User testUser;

    @BeforeEach
    void setUp() {
        // Initialize your test user here
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFailedLoginAttempts(0);
        testUser.setLocked(false);

        // Manually construct the service with mocked dependencies
        loginAttemptService = new LoginAttemptService(userRepository);
        loginAttemptService.setMaxFailedLoginAttempts(failedLoginAttempts);
        loginAttemptService.setAccountLockoutDuration(accountLockoutDuration);
    }

    @Test
    void loginSucceeded_resetsFailedAttempts() {
        when(userRepository.findByEmail(anyString())).thenReturn(testUser);

        loginAttemptService.loginSucceeded(testUser.getEmail());

        assertEquals(0, testUser.getFailedLoginAttempts());
        assertFalse(testUser.isLocked());
        assertNull(testUser.getLockedDate());
        verify(userRepository).save(testUser);
    }

    @Test
    void loginFailed_incrementsFailedAttempts() {
        when(userRepository.findByEmail(anyString())).thenReturn(testUser);

        for (int i = 1; i <= failedLoginAttempts; i++) {
            loginAttemptService.loginFailed(testUser.getEmail());
        }

        assertEquals(failedLoginAttempts, testUser.getFailedLoginAttempts());
        assertTrue(testUser.isLocked());
        assertNotNull(testUser.getLockedDate());
        verify(userRepository, times(failedLoginAttempts)).save(testUser);
    }

    @Test
    void isLocked_returnsTrueWhenUserIsLocked() {
        testUser.setLocked(true);
        testUser.setLockedDate(new Date());

        when(userRepository.findByEmail(anyString())).thenReturn(testUser);

        assertTrue(loginAttemptService.isLocked(testUser.getEmail()));
    }

    @Test
    void isLocked_returnsFalseWhenUserIsNotLocked() {
        when(userRepository.findByEmail(anyString())).thenReturn(testUser);

        assertFalse(loginAttemptService.isLocked(testUser.getEmail()));
    }

    @Test
    void isLocked_unlocksUserAfterLockoutDuration() {
        // Set the user as locked with a lock date before the lockout duration
        testUser.setLocked(true);
        testUser.setLockedDate(new Date(System.currentTimeMillis() - (accountLockoutDuration + 1) * 60 * 1000));

        when(userRepository.findByEmail(anyString())).thenReturn(testUser);

        assertFalse(loginAttemptService.isLocked(testUser.getEmail()));
        assertFalse(testUser.isLocked());
        assertNull(testUser.getLockedDate());
        assertEquals(0, testUser.getFailedLoginAttempts());
        verify(userRepository).save(testUser);
    }

    // Additional tests can be written for edge cases and exception handling
}
