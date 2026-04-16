package com.jaegokeeper.auth;

import com.jaegokeeper.auth.controller.SocialController;
import com.jaegokeeper.auth.dto.SessionResponse;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.auth.service.SocialService;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class SocialControllerWebTest {

    @Mock
    private SocialService socialService;

    @Mock
    private SessionService sessionService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        SocialController controller = new SocialController(socialService, sessionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void 소셜로그인_provider누락_400() throws Exception {
        String body = "{\"accessToken\":\"token-1\"}";

        mockMvc.perform(post("/auth/social/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(socialService, sessionService);
    }

    @Test
    public void 소셜로그인_accessToken공백_400() throws Exception {
        String body = "{\"provider\":\"KAKAO\",\"accessToken\":\"\"}";

        mockMvc.perform(post("/auth/social/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(socialService, sessionService);
    }

    @Test
    public void 소셜로그인_provider값오류_400_PROVIDER_INVALID() throws Exception {
        String body = "{\"provider\":\"FACEBOOK\",\"accessToken\":\"token-1\"}";

        mockMvc.perform(post("/auth/social/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"PROVIDER_INVALID\"")));

        verifyNoInteractions(socialService, sessionService);
    }

    @Test
    public void 소셜로그인_정상요청_200() throws Exception {
        String body = "{\"provider\":\"KAKAO\",\"accessToken\":\"token-1\"}";
        SessionResponse response = SessionResponse.builder()
                .userId(25)
                .storeId(22)
                .userName("이승환")
                .provider("KAKAO")
                .build();

        when(socialService.complete(eq("KAKAO"), eq("token-1"))).thenReturn(25);
        when(sessionService.createSession(eq(25), eq("KAKAO"), any(HttpServletRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/social/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"userId\":25")))
                .andExpect(content().string(containsString("\"storeId\":22")))
                .andExpect(content().string(containsString("\"provider\":\"KAKAO\"")));

        verify(socialService).complete("KAKAO", "token-1");
        verify(sessionService).createSession(eq(25), eq("KAKAO"), any(HttpServletRequest.class));
    }
}
