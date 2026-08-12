package com.jaegokeeper.alba;

import com.jaegokeeper.alba.controller.StoreAlbaController;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.service.AlbaService;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.utils.LoginUserArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.auth.utils.SessionInterceptor;
import com.jaegokeeper.exception.GlobalExceptionHandler;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
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

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        StoreAlbaController controller = new StoreAlbaController(albaService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new SessionInterceptor(new ObjectMapper()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .build();
    }

    @Test
    public void 스토어알바목록_미로그인_401() throws Exception {
        mockMvc.perform(get("/stores/albas"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));

        verifyNoInteractions(albaService);
    }

    // storeId가 URL에 없어져서 "다른 스토어로 요청" 시나리오 자체를 구성할 수 없다.
    // (검증이 빠진 게 아니라 URL로 다른 점포를 지목하는 공격 표면 자체가 사라졌다.)

    @Test
    public void 스토어알바목록_같은스토어_200() throws Exception {
        MockHttpSession session = loginSession(1);

        AlbaListResponse one = new AlbaListResponse();
        one.setAlbaId(11);
        one.setAlbaName("홍길동");

        doReturn(List.of(one))
                .when(albaService)
                .getAllAlbaList(any(LoginContext.class));

        mockMvc.perform(get("/stores/albas").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"albaId\":11")));
    }

    @Test
    public void 스토어알바삭제_미로그인_401() throws Exception {
        mockMvc.perform(delete("/stores/albas/11"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));

        verifyNoInteractions(albaService);
    }

    @Test
    public void 스토어알바수정_이메일형식오류_400() throws Exception {
        MockHttpSession session = loginSession(1);
        String body = "{\"albaEmail\":\"not-an-email\"}";

        mockMvc.perform(put("/stores/albas/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(albaService);
    }

    private MockHttpSession loginSession(int storeId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("login", new LoginContext(100, storeId, "tester", "LOCAL"));
        return session;
    }
}
