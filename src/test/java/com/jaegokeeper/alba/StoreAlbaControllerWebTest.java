package com.jaegokeeper.alba;

import com.jaegokeeper.alba.controller.StoreAlbaController;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.service.AlbaService;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.utils.SessionInterceptor;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import com.jaegokeeper.image.service.ImageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.FORBIDDEN;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class StoreAlbaControllerWebTest {

    @Mock
    private AlbaService albaService;

    @Mock
    private ImageService imageService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        StoreAlbaController controller = new StoreAlbaController(albaService, imageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new SessionInterceptor())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void 스토어알바목록_미로그인_401() throws Exception {
        mockMvc.perform(get("/stores/1/albas"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));

        verifyNoInteractions(albaService);
    }

    @Test
    public void 스토어알바목록_다른스토어_403() throws Exception {
        MockHttpSession session = loginSession(1);
        doThrow(new BusinessException(FORBIDDEN))
                .when(albaService)
                .getAllAlbaList(any(LoginContext.class), intThat(v -> v == 2));

        mockMvc.perform(get("/stores/2/albas").session(session))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("\"code\":\"FORBIDDEN\"")));
    }

    @Test
    public void 스토어알바목록_같은스토어_200() throws Exception {
        MockHttpSession session = loginSession(1);

        AlbaListResponse one = new AlbaListResponse();
        one.setAlbaId(11);
        one.setAlbaName("홍길동");

        doReturn(List.of(one))
                .when(albaService)
                .getAllAlbaList(any(LoginContext.class), intThat(v -> v == 1));

        mockMvc.perform(get("/stores/1/albas").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"albaId\":11")));
    }

    @Test
    public void 스토어알바삭제_미로그인_401() throws Exception {
        mockMvc.perform(delete("/stores/1/albas/11"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));

        verifyNoInteractions(albaService);
    }

    @Test
    public void 스토어알바수정_다른스토어_403() throws Exception {
        MockHttpSession session = loginSession(1);

        doThrow(new BusinessException(FORBIDDEN))
                .when(albaService)
                .updateAlba(any(LoginContext.class), intThat(v -> v == 2), any());

        mockMvc.perform(put("/stores/2/albas/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .session(session))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("\"code\":\"FORBIDDEN\"")));
    }

    private MockHttpSession loginSession(int storeId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("login", new LoginContext(100, storeId, "tester", "LOCAL"));
        return session;
    }
}
