package com.devstaq.auth.service;

import com.devstaq.auth.dto.UserDto;
import com.devstaq.auth.exceptions.UserAlreadyExistException;
import com.devstaq.auth.persistence.model.Role;
import com.devstaq.auth.persistence.model.User;
import com.devstaq.auth.persistence.repository.PasswordResetTokenRepository;
import com.devstaq.auth.persistence.repository.RoleRepository;
import com.devstaq.auth.persistence.repository.UserRepository;
import com.devstaq.auth.persistence.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
//@Disabled("Temporarily disabled due to OAuth2 dependency issues")
public class UserServiceTest {

    private static final String USER_ROLE_NAME = "ROLE_USER";
    @Mock
    private UserRepository userRepository;
    @Mock
    private VerificationTokenRepository tokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private SessionRegistry sessionRegistry;
    @Mock
    public UserEmailService userEmailService;
    @Mock
    public UserVerificationService userVerificationService;
    @Mock
    private DSUserDetailsService dsUserDetailsService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuthorityService authorityService;

    private UserService userService;
    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("testFirstName");
        testUser.setLastName("testLastName");
        testUser.setPassword("testPassword");
        testUser.setRoles(Collections.singletonList(new Role("ROLE_USER")));
        testUser.setEnabled(true);

        testUserDto = new UserDto();
        testUserDto.setEmail("test@example.com");
        testUserDto.setFirstName("testFirstName");
        testUserDto.setLastName("testLastName");
        testUserDto.setPassword("testPassword");
        testUserDto.setRole(1);

        userService = new UserService(userRepository, tokenRepository, passwordTokenRepository, passwordEncoder, roleRepository, sessionRegistry,
                userEmailService, userVerificationService, authorityService, dsUserDetailsService, eventPublisher);
    }

    @Test
    void registerNewUserAccount_returnsUserWhenUserIsNew() {
        when(passwordEncoder.encode(testUser.getPassword())).thenReturn(testUser.getPassword());
        when(roleRepository.findByName(USER_ROLE_NAME)).thenReturn(new Role(USER_ROLE_NAME));
        when(userRepository.save(testUser)).thenReturn(testUser);
        User saved = userService.registerNewUserAccount(testUserDto);
        Assertions.assertEquals(saved, testUser);
    }

    @Test
    void registerNewUserAccount_throwsExceptionWhenUserExist() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(testUser);
        Assertions.assertThrows(UserAlreadyExistException.class, () -> userService.registerNewUserAccount(testUserDto));
    }

    @Test
    void findByEmail_returnsUserWhenEmailExist() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(testUser);
        User found = userService.findUserByEmail(testUser.getEmail());
        Assertions.assertEquals(found, testUser);
    }


    @Test
    void checkIfValidOldPassword_returnTrueIfValid() {
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        Assertions.assertTrue(userService.checkIfValidOldPassword(testUser, testUser.getPassword()));
    }

    // Tests temporarily disabled until OAuth2 dependency issue is resolved
    // @Test
    // void checkIfValidOldPassword_returnFalseIfInvalid() {
    // when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    // Assertions.assertFalse(userService.checkIfValidOldPassword(testUser, "wrongPassword"));
    // }
    //
    // @Test
    // void changeUserPassword_encodesAndSavesNewPassword() {
    // String newPassword = "newTestPassword";
    // String encodedPassword = "encodedNewPassword";
    //
    // when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
    // when(userRepository.save(any(User.class))).thenReturn(testUser);
    //
    // userService.changeUserPassword(testUser, newPassword);
    //
    // Assertions.assertEquals(encodedPassword, testUser.getPassword());
    // }

}
