package com.jaegokeeper.user;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.utils.LoginUserArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.auth.utils.SessionInterceptor;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import com.jaegokeeper.user.controller.UserController;
import com.jaegokeeper.user.dto.UserUpdateRequest;
import com.jaegokeeper.user.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerWebTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new SessionInterceptor(new ObjectMapper()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .build();
    }

    @Test
    public void 유저수정_미로그인_401() throws Exception {
        String body = "{\"userMail\":\"user@example.com\",\"userPhone\":\"010-1234-5678\"}";

        mockMvc.perform(put("/users/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));

        verifyNoInteractions(userService);
    }

    @Test
    public void 유저수정_이메일형식오류_400() throws Exception {
        MockHttpSession session = loginSession(1);
        String body = "{\"userMail\":\"invalid-mail\",\"userPhone\":\"010-1234-5678\"}";

        mockMvc.perform(put("/users/100")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(userService);
    }

    @Test
    public void 유저수정_전화번호길이초과_400() throws Exception {
        MockHttpSession session = loginSession(1);
        String body = "{\"userMail\":\"user@example.com\",\"userPhone\":\"010-1234-5678-9999-0000-1111\"}";

        mockMvc.perform(put("/users/100")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(userService);
    }

    @Test
    public void 유저수정_정상요청_204() throws Exception {
        MockHttpSession session = loginSession(1);
        String body = "{\"userMail\":\"user@example.com\",\"userPhone\":\"010-1234-5678\"}";

        mockMvc.perform(put("/users/100")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        verify(userService).updateUser(any(LoginContext.class), eq(100), any(UserUpdateRequest.class));
    }

    @Test
    public void 유저수정_userId검증은서비스책임_컨트롤러는경로id전달() throws Exception {
        MockHttpSession session = loginSession(1); // login.userId=100
        String body = "{\"userMail\":\"user@example.com\",\"userPhone\":\"010-1234-5678\"}";

        mockMvc.perform(put("/users/200")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        // 컨트롤러는 경로 userId를 그대로 서비스로 전달한다.
        verify(userService).updateUser(any(LoginContext.class), eq(200), any(UserUpdateRequest.class));
    }

    private MockHttpSession loginSession(int storeId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("login", new LoginContext(100, storeId, "tester", "LOCAL"));
        return session;
    }
}
