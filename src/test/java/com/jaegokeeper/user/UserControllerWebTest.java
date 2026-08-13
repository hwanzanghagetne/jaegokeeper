package com.jaegokeeper.user;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import com.jaegokeeper.user.controller.UserController;
import com.jaegokeeper.user.dto.UserUpdateRequest;
import com.jaegokeeper.user.service.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "미로그인 401"은 SecurityFilterChain이 담당하며 standaloneSetup()으로는 검증 불가하므로,
 * Security 필터체인 통합 테스트/LoginRequiredEntryPointTest가 그 책임을 진다.
 * 여기서는 로그인된 상태의 해피패스만 다룬다.
 *
 * 로그인 상태는 SecurityContextHolder에 직접 Authentication을 설정해서 시뮬레이션한다
 * (SecurityMockMvcRequestPostProcessors.authentication()은 필터 체인이 있어야 동작하므로
 * 필터가 없는 standaloneSetup()에서는 쓸 수 없다).
 */
@RunWith(MockitoJUnitRunner.class)
public class UserControllerWebTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void 유저수정_이메일형식오류_400() throws Exception {
        login(1);
        String body = "{\"userMail\":\"invalid-mail\",\"userPhone\":\"010-1234-5678\"}";

        mockMvc.perform(put("/users/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(userService);
    }

    @Test
    public void 유저수정_전화번호길이초과_400() throws Exception {
        login(1);
        String body = "{\"userMail\":\"user@example.com\",\"userPhone\":\"010-1234-5678-9999-0000-1111\"}";

        mockMvc.perform(put("/users/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(userService);
    }

    @Test
    public void 유저수정_정상요청_204() throws Exception {
        login(1);
        String body = "{\"userMail\":\"user@example.com\",\"userPhone\":\"010-1234-5678\"}";

        mockMvc.perform(put("/users/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        verify(userService).updateUser(any(LoginContext.class), eq(100), any(UserUpdateRequest.class));
    }

    @Test
    public void 유저수정_userId검증은서비스책임_컨트롤러는경로id전달() throws Exception {
        login(1); // login.userId=100
        String body = "{\"userMail\":\"user@example.com\",\"userPhone\":\"010-1234-5678\"}";

        mockMvc.perform(put("/users/200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        // 컨트롤러는 경로 userId를 그대로 서비스로 전달한다.
        verify(userService).updateUser(any(LoginContext.class), eq(200), any(UserUpdateRequest.class));
    }

    private static void login(int storeId) {
        LoginContext loginContext = new LoginContext(100, storeId, "tester", "LOCAL");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of()));
    }
}
