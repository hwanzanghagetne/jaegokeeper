package com.jaegokeeper.alba;

import com.jaegokeeper.alba.controller.StoreAlbaController;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.service.AlbaService;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.GlobalExceptionHandler;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "미로그인 401"은 SecurityFilterChain이 담당하며 standaloneSetup()으로는 검증 불가하므로,
 * Security 필터체인 통합 테스트/LoginRequiredEntryPointTest가 그 책임을 진다.
 * 여기서는 로그인된 상태의 해피패스만 다룬다.
 *
 * storeId가 URL에 없어져서 "다른 스토어로 요청" 시나리오 자체를 구성할 수 없다.
 * (검증이 빠진 게 아니라 URL로 다른 점포를 지목하는 공격 표면 자체가 사라졌다.)
 *
 * 로그인 상태는 SecurityContextHolder에 직접 Authentication을 설정해서 시뮬레이션한다
 * (SecurityMockMvcRequestPostProcessors.authentication()은 필터 체인이 있어야 동작하므로
 * 필터가 없는 standaloneSetup()에서는 쓸 수 없다).
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreAlbaControllerWebTest {

    @Mock
    private AlbaService albaService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        StoreAlbaController controller = new StoreAlbaController(albaService);
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
    public void 스토어알바목록_같은스토어_200() throws Exception {
        AlbaListResponse one = new AlbaListResponse();
        one.setAlbaId(11);
        one.setAlbaName("홍길동");

        doReturn(List.of(one))
                .when(albaService)
                .getAllAlbaList(any(LoginContext.class));

        login(1);

        mockMvc.perform(get("/stores/albas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"albaId\":11")));
    }

    @Test
    public void 스토어알바수정_이메일형식오류_400() throws Exception {
        login(1);
        String body = "{\"albaEmail\":\"not-an-email\"}";

        mockMvc.perform(put("/stores/albas/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(albaService);
    }

    private static void login(int storeId) {
        LoginContext loginContext = new LoginContext(100, storeId, "tester", "LOCAL");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of()));
    }
}
