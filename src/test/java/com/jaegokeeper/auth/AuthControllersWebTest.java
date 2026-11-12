package com.jaegokeeper.auth;

import com.jaegokeeper.auth.controller.LocalController;
import com.jaegokeeper.auth.dto.SessionResponse;
import com.jaegokeeper.auth.service.LocalService;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.email.controller.EmailAuthController;
import com.jaegokeeper.email.service.EmailAuthService;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import com.jaegokeeper.onboarding.controller.OnboardingController;
import com.jaegokeeper.onboarding.dto.OwnerSignUpRequest;
import com.jaegokeeper.onboarding.dto.OwnerSignUpResponse;
import com.jaegokeeper.onboarding.service.OnboardingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
public class AuthControllersWebTest {

    @Mock
    private LocalService localService;

    @Mock
    private SessionService sessionService;

    @Mock
    private EmailAuthService emailAuthService;

    @Mock
    private OnboardingService onboardingService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new LocalController(localService, sessionService),
                        new EmailAuthController(emailAuthService),
                        new OnboardingController(onboardingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String ownerSignUpBody(String password, boolean withStore) {
        String store = withStore ? ",\"store\":{\"storeName\":\"테스트점\"}" : "";
        return "{\"account\":{\"email\":\"owner@test.com\",\"password\":\"" + password
                + "\",\"userName\":\"사장\"}" + store + "}";
    }

    @Test
    public void 로그인_이메일_형식이_틀리면_400() throws Exception {
        mockMvc.perform(post("/auth/local/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(localService, sessionService);
    }

    @Test
    public void 로그인_성공시_세션_정보_응답() throws Exception {
        when(localService.login(any())).thenReturn(7);
        when(sessionService.createSession(eq(7), eq("LOCAL"), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(SessionResponse.builder().userId(7).storeId(3).userName("사장").provider("LOCAL").build());

        mockMvc.perform(post("/auth/local/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"userId\":7")))
                .andExpect(content().string(containsString("\"provider\":\"LOCAL\"")));
    }

    @Test
    public void 인증번호_발송_이메일_형식이_틀리면_400() throws Exception {
        mockMvc.perform(post("/auth/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(emailAuthService);
    }

    @Test
    public void 인증번호_발송_성공시_204() throws Exception {
        mockMvc.perform(post("/auth/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner@test.com\"}"))
                .andExpect(status().isNoContent());

        verify(emailAuthService).sendCode("owner@test.com");
    }

    @Test
    public void 인증번호_확인_6자리_숫자가_아니면_400() throws Exception {
        mockMvc.perform(post("/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner@test.com\",\"code\":\"12ab56\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("인증번호는 6자리 숫자여야 합니다.")));

        verifyNoInteractions(emailAuthService);
    }

    @Test
    public void 점주가입_점포_정보가_없으면_400() throws Exception {
        mockMvc.perform(post("/onboarding/owner-signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ownerSignUpBody("password123", false)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("store 정보는 필수입니다")));

        verifyNoInteractions(onboardingService);
    }

    @Test
    public void 점주가입_한글_비밀번호가_72바이트를_넘으면_400() throws Exception {
        mockMvc.perform(post("/onboarding/owner-signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ownerSignUpBody("가".repeat(25), true)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("비밀번호는 72바이트 이하여야 합니다.")));

        verifyNoInteractions(onboardingService);
    }

    @Test
    public void 점주가입_한글_비밀번호가_72바이트_이하면_통과() throws Exception {
        when(onboardingService.ownerSignUp(any(OwnerSignUpRequest.class)))
                .thenReturn(OwnerSignUpResponse.builder().userId(1).storeId(2).userName("사장").storeName("테스트점").build());

        mockMvc.perform(post("/onboarding/owner-signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ownerSignUpBody("가".repeat(24), true)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"storeId\":2")));
    }

    @Test
    public void 소셜_점주가입_가입토큰이_없으면_400() throws Exception {
        mockMvc.perform(post("/onboarding/social-owner-signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":{\"userName\":\"사장\"},\"store\":{\"storeName\":\"테스트점\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("소셜 가입 토큰은 필수입니다")));

        verifyNoInteractions(onboardingService);
    }
}
