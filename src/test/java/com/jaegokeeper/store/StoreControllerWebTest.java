package com.jaegokeeper.store;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import com.jaegokeeper.store.controller.StoreController;
import com.jaegokeeper.store.service.StoreService;
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
public class StoreControllerWebTest {

    @Mock
    private StoreService storeService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        StoreController controller = new StoreController(storeService);
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
    public void 점포수정_매장명공백_400() throws Exception {
        login(1);
        String body = "{\"storeName\":\"\",\"storeAdd1\":\"서울\",\"storeAdd2\":\"101호\",\"storeTel\":\"02-0000-0000\"}";

        mockMvc.perform(put("/stores/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(storeService);
    }

    @Test
    public void 점포수정_정상요청_204() throws Exception {
        login(1);
        String body = "{\"storeName\":\"자초단\",\"storeAdd1\":\"서울\",\"storeAdd2\":\"101호\",\"storeTel\":\"02-0000-0000\"}";

        mockMvc.perform(put("/stores/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        verify(storeService).updateStore(any(LoginContext.class), any());
    }

    private static void login(int storeId) {
        LoginContext loginContext = new LoginContext(100, storeId, "tester", "LOCAL");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of()));
    }
}
