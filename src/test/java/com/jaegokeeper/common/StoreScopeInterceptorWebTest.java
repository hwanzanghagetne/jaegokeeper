package com.jaegokeeper.common;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import com.jaegokeeper.item.controller.ItemController;
import com.jaegokeeper.item.dto.response.ItemListResponse;
import com.jaegokeeper.item.service.ItemService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * storeId는 더 이상 URL에 없고 세션(LoginContext)만 신뢰하므로, 이 테스트는
 * "로그인 여부"만 검증한다. 예전에 있던 "다른 storeId로 요청하면 403" 테스트는
 * URL에 storeId 자체가 없어져서 그 시나리오를 구성할 수 없어 삭제했다 —
 * 검증이 빠진 게 아니라 그 공격 표면 자체가 사라진 것이다.
 *
 * "미로그인 401"은 이제 SecurityFilterChain(서블릿 필터)이 담당하는데,
 * standaloneSetup()은 필터 체인을 안 태우므로 이 테스트로는 검증할 수 없다.
 * 그 책임은 Security 필터체인 통합 테스트(ItemController 스파이크)와
 * LoginRequiredEntryPointTest가 진다. 여기서는 로그인된 상태의 해피패스만 다룬다.
 *
 * 로그인 상태 시뮬레이션은 SecurityMockMvcRequestPostProcessors.authentication()이 아니라
 * SecurityContextHolder에 직접 설정하는 방식을 쓴다 — 그 포스트프로세서는 SecurityContextRepository를
 * 거쳐 "필터가 나중에 읽어가는" 구조라 실제 필터 체인이 없는 standaloneSetup()에서는 동작하지 않는다
 * (AuthenticationPrincipalArgumentResolver는 SecurityContextHolder를 직접 읽으므로 이렇게 해야 함).
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreScopeInterceptorWebTest {

    @Mock
    private ItemService itemService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        ItemController controller = new ItemController(itemService);
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
    public void 스토어리소스_로그인시_200() throws Exception {
        doReturn(PageResponse.of(Collections.<ItemListResponse>emptyList(), 1, 10, 0))
                .when(itemService)
                .getItemList(any(LoginContext.class), any());

        login(1);

        mockMvc.perform(get("/stores/items"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"content\":[]")));
    }

    private static void login(int storeId) {
        LoginContext loginContext = new LoginContext(100, storeId, "tester", "LOCAL");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of()));
    }
}
