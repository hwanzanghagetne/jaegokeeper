package com.jaegokeeper.board;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.board.controller.BoardController;
import com.jaegokeeper.board.dto.response.BoardListResponse;
import com.jaegokeeper.board.service.BoardService;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.exception.GlobalExceptionHandler;
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
 * "미로그인 401"은 SecurityFilterChain이 담당하며 standaloneSetup()으로는 검증 불가하므로,
 * Security 필터체인 통합 테스트/LoginRequiredEntryPointTest가 그 책임을 진다.
 * 여기서는 로그인된 상태의 해피패스만 다룬다.
 *
 * 로그인 상태는 SecurityContextHolder에 직접 Authentication을 설정해서 시뮬레이션한다
 * (SecurityMockMvcRequestPostProcessors.authentication()은 필터 체인이 있어야 동작하므로
 * 필터가 없는 standaloneSetup()에서는 쓸 수 없다).
 */
@RunWith(MockitoJUnitRunner.class)
public class BoardControllerWebTest {

    @Mock
    private BoardService boardService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        BoardController controller = new BoardController(boardService);
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
    public void 게시글목록_로그인시_200() throws Exception {
        doReturn(PageResponse.of(Collections.<BoardListResponse>emptyList(), 1, 10, 0))
                .when(boardService)
                .getBoardList(any(LoginContext.class), any());

        login(1);

        mockMvc.perform(get("/stores/boards"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"content\":[]")));
    }

    private static void login(int storeId) {
        LoginContext loginContext = new LoginContext(100, storeId, "tester", "LOCAL");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of()));
    }
}
