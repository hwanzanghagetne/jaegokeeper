package com.jaegokeeper.schedule;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import com.jaegokeeper.schedule.controller.StoreScheduleController;
import com.jaegokeeper.schedule.dto.ScheduleListResponse;
import com.jaegokeeper.schedule.service.ScheduleService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void 스토어스케줄조회_같은스토어_200() throws Exception {
        ScheduleListResponse row = new ScheduleListResponse();
        row.setScheduleId(21);
        row.setAlbaId(7);

        doReturn(List.of(row))
                .when(scheduleService)
                .getScheduleListByDate(any(LoginContext.class), eq("2026-04-08"));

        login(1);

        mockMvc.perform(get("/stores/schedules").param("date", "2026-04-08"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"scheduleId\":21")))
                .andExpect(content().string(containsString("\"albaId\":7")));
    }

    @Test
    public void 출근기록_albaId누락_400() throws Exception {
        login(1);

        mockMvc.perform(post("/stores/schedules/workin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(scheduleService);
    }

    private static void login(int storeId) {
        LoginContext loginContext = new LoginContext(100, storeId, "tester", "LOCAL");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of()));
    }
}
