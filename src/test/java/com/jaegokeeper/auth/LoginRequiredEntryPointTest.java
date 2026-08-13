package com.jaegokeeper.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.auth.config.LoginRequiredEntryPoint;
import com.jaegokeeper.exception.ErrorCode;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoginRequiredEntryPointTest {

    private LoginRequiredEntryPoint entryPoint;

    @Before
    public void setUp() {
        entryPoint = new LoginRequiredEntryPoint(new ObjectMapper());
    }

    @Test
    public void 비로그인_401_LOGIN_REQUIRED_JSON_반환() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, null);

        assertEquals(401, response.getStatus());
        assertEquals(StandardCharsets.UTF_8.name(), response.getCharacterEncoding());
        assertTrue(response.getContentType().startsWith("application/json"));

        String body = response.getContentAsString();
        assertTrue(body.contains("\"code\":\"LOGIN_REQUIRED\""));
        assertTrue(body.contains(ErrorCode.LOGIN_REQUIRED.getMessage()));
    }
}
