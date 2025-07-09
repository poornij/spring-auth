package com.devstaq.auth.api;

import com.devstaq.auth.api.data.ApiTestData;
import com.devstaq.auth.api.data.DataStatus;
import com.devstaq.auth.api.data.Response;
import com.devstaq.auth.api.helper.AssertionsHelper;
import com.devstaq.auth.api.provider.ApiTestRegistrationArgumentsProvider;
import com.devstaq.auth.api.provider.holder.ApiTestArgumentsHolder;
import com.devstaq.auth.dto.UserDto;
import com.devstaq.auth.jdbc.Jdbc;
import com.devstaq.auth.persistence.model.User;
import com.devstaq.auth.service.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.devstaq.auth.api.helper.ApiTestHelper.buildUrlEncodedFormEntity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("Temporarily disabled due to OAuth2 dependency issues")
public class UserApiTest extends BaseApiTest {
    private static final String URL = "/user";

    @Autowired
    private UserService userService;

    private static final UserDto baseTestUser = ApiTestData.BASE_TEST_USER;

    @AfterAll
    public static void afterAll() {
        Jdbc.deleteTestUser(baseTestUser);
    }

    /**
     *
     * @param argumentsHolder
     * @throws Exception testing with three params: new user data, exist user data and invalid user data
     */
    @ParameterizedTest
    @ArgumentsSource(ApiTestRegistrationArgumentsProvider.class)
    @Order(1)
    // correctly run separately
    public void registerUserAccount(ApiTestArgumentsHolder argumentsHolder) throws Exception {
        ResultActions action = perform(MockMvcRequestBuilders.post(URL + "/registration").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(buildUrlEncodedFormEntity(argumentsHolder.getUserDto())));

        if (argumentsHolder.getStatus() == DataStatus.NEW) {
            action.andExpect(status().isOk());
        }
        if (argumentsHolder.getStatus() == DataStatus.EXIST) {
            action.andExpect(status().isConflict());
        }
        if (argumentsHolder.getStatus() == DataStatus.INVALID) {
            action.andExpect(status().is5xxServerError());
        }

        MockHttpServletResponse actual = action.andReturn().getResponse();
        Response excepted = argumentsHolder.getResponse();
        AssertionsHelper.compareResponses(actual, excepted);
    }

    @Test
    @Order(2)
    public void resetPassword() throws Exception {
        ResultActions action = perform(MockMvcRequestBuilders.post(URL + "/resetPassword").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(buildUrlEncodedFormEntity(baseTestUser))).andExpect(status().isOk());

        MockHttpServletResponse actual = action.andReturn().getResponse();
        Response excepted = ApiTestData.resetPassword();
        AssertionsHelper.compareResponses(actual, excepted);
    }

    // Tests temporarily disabled until OAuth2 dependency issue is resolved
    // /**
    // * Tests the update password functionality with valid and invalid password combinations.
    // *
    // * @param argumentsHolder Contains test data for password updates (valid/invalid scenarios)
    // * @throws Exception if any error occurs during test execution
    // */
    // @ParameterizedTest
    // @ArgumentsSource(ApiTestUpdatePasswordArgumentsProvider.class)
    // @Order(3)
    // public void updatePassword(ApiTestArgumentsHolder argumentsHolder) throws Exception {
    // // Register and login test user first
    // login(baseTestUser);
    //
    // PasswordDto passwordDto = argumentsHolder.getPasswordDto();
    //
    // ResultActions action = perform(MockMvcRequestBuilders.post(URL + "/updatePassword")
    // .contentType(MediaType.APPLICATION_FORM_URLENCODED)
    // .content(buildUrlEncodedFormEntity(passwordDto)));
    //
    // if (argumentsHolder.getStatus() == DataStatus.VALID) {
    // action.andExpect(status().isOk());
    // }
    // if (argumentsHolder.getStatus() == DataStatus.INVALID) {
    // action.andExpect(status().isBadRequest());
    // }
    //
    // MockHttpServletResponse actual = action.andReturn().getResponse();
    // Response expected = argumentsHolder.getResponse();
    // AssertionsHelper.compareResponses(actual, expected);
    // }


    protected void login(UserDto userDto) {
        User user;
        if ((user = userService.findUserByEmail(userDto.getEmail())) == null) {
            user = userService.registerNewUserAccount(userDto);
        }
        userService.authWithoutPassword(user);
    }


}
