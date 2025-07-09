package com.devstaq.auth.api.helper;

import com.devstaq.auth.api.data.Response;
import com.devstaq.auth.json.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.springframework.mock.web.MockHttpServletResponse;


public class AssertionsHelper {
    public static void compareResponses(MockHttpServletResponse servletResponse, Response excepted) throws Exception{
        Response actual = JsonUtil.readValue(servletResponse.getContentAsString(), Response.class);
        Assertions.assertEquals(actual, excepted);
    }
}
