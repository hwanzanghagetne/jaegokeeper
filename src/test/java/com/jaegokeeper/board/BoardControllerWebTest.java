package com.jaegokeeper.board;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.utils.LoginUserArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.auth.utils.SessionInterceptor;
import com.jaegokeeper.board.controller.BoardController;
import com.jaegokeeper.board.dto.response.BoardListResponse;
import com.jaegokeeper.board.service.BoardService;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 리팩터링 전에는 BoardController가 @LoginUser를 받지 않고 서비스 계층 검증도
 * 없어서 이 웹 테스트 자체가 없었다(SessionInterceptor의 URL storeId 비교 하나에만
 * 의존). storeId를 세션 기준으로 통일하면서 다른 컨트롤러와 같은 최소 시나리오
 * (미로그인 401 / 로그인 시 200)를 신규로 추가한다.
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
                .addInterceptors(new SessionInterceptor(new ObjectMapper()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .build();
    }

    @Test
    public void 게시글목록_미로그인_401() throws Exception {
        mockMvc.perform(get("/stores/boards"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));

        verifyNoInteractions(boardService);
    }

    @Test
    public void 게시글목록_로그인시_200() throws Exception {
        MockHttpSession session = loginSession(1);

        doReturn(PageResponse.of(Collections.<BoardListResponse>emptyList(), 1, 10, 0))
                .when(boardService)
                .getBoardList(any(LoginContext.class), any());

        mockMvc.perform(get("/stores/boards").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"content\":[]")));
    }

    private MockHttpSession loginSession(int storeId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("login", new LoginContext(100, storeId, "tester", "LOCAL"));
        return session;
    }
}
