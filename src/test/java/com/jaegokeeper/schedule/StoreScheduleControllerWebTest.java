package com.jaegokeeper.schedule;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.utils.LoginUserArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.auth.utils.SessionInterceptor;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import com.jaegokeeper.schedule.controller.StoreScheduleController;
import com.jaegokeeper.schedule.dto.ScheduleListResponse;
import com.jaegokeeper.schedule.service.ScheduleService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class StoreScheduleControllerWebTest {

    @Mock
    private ScheduleService scheduleService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        StoreScheduleController controller = new StoreScheduleController(scheduleService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new SessionInterceptor(new ObjectMapper()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .build();
    }

    @Test
    public void 스토어스케줄조회_미로그인_401() throws Exception {
        mockMvc.perform(get("/stores/1/schedules").param("date", "2026-04-08"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));

        verifyNoInteractions(scheduleService);
    }

    @Test
    public void 스토어스케줄조회_다른스토어_403() throws Exception {
        MockHttpSession session = loginSession(1);

        mockMvc.perform(get("/stores/2/schedules")
                        .param("date", "2026-04-08")
                        .session(session))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("\"code\":\"FORBIDDEN\"")));

        verifyNoInteractions(scheduleService);
    }

    @Test
    public void 스토어스케줄조회_같은스토어_200() throws Exception {
        MockHttpSession session = loginSession(1);

        ScheduleListResponse row = new ScheduleListResponse();
        row.setScheduleId(21);
        row.setAlbaId(7);

        doReturn(List.of(row))
                .when(scheduleService)
                .getScheduleListByDate(any(LoginContext.class), intThat(v -> v == 1), eq("2026-04-08"));

        mockMvc.perform(get("/stores/1/schedules")
                        .param("date", "2026-04-08")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"scheduleId\":21")))
                .andExpect(content().string(containsString("\"albaId\":7")));
    }

    @Test
    public void 스토어스케줄수정_미로그인_401() throws Exception {
        mockMvc.perform(put("/stores/1/schedules/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));

        verifyNoInteractions(scheduleService);
    }

    @Test
    public void 스토어스케줄수정_다른스토어_403() throws Exception {
        MockHttpSession session = loginSession(1);

        mockMvc.perform(put("/stores/2/schedules/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .session(session))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("\"code\":\"FORBIDDEN\"")));

        verifyNoInteractions(scheduleService);
    }

    @Test
    public void 출근기록_albaId누락_400() throws Exception {
        MockHttpSession session = loginSession(1);

        mockMvc.perform(post("/stores/1/schedules/workin")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(scheduleService);
    }

    private MockHttpSession loginSession(int storeId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("login", new LoginContext(100, storeId, "tester", "LOCAL"));
        return session;
    }
}
