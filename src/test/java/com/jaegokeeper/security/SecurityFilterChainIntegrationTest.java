package com.jaegokeeper.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.auth.config.LoginRequiredEntryPoint;
import com.jaegokeeper.auth.config.SecurityConfig;
import com.jaegokeeper.auth.controller.SessionController;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import com.jaegokeeper.item.controller.ItemController;
import com.jaegokeeper.item.dto.response.ItemListResponse;
import com.jaegokeeper.item.service.ItemService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring Security 전환의 "스파이크" 테스트 — root-context.xml/servlet-context.xml은
 * 전혀 로드하지 않는(DB/메일 등 환경변수 플레이스홀더 문제를 피하는) 최소
 * WebApplicationContext에 실제 SecurityConfig/LoginRequiredEntryPoint만 등록해서,
 * springSecurityFilterChain + @AuthenticationPrincipal + AuthenticationEntryPoint가
 * 실제 서블릿 필터 체인을 통해 end-to-end로 동작하는지 검증한다.
 *
 * standaloneSetup()과 달리 webAppContextSetup() + springSecurity()는 진짜 필터 체인을
 * 태우므로, SecurityMockMvcRequestPostProcessors.authentication()이 여기서는 정상 동작한다.
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityFilterChainIntegrationTest {

    @Mock
    private ItemService itemService;

    private MockMvc mockMvc;

    @Configuration
    @EnableWebMvc
    static class MinimalTestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Before
    public void setUp() {
        // AnnotationConfigWebApplicationContext는 refresh() 전에는 내부 BeanFactory에
        // 접근할 수 없어서(AbstractRefreshableWebApplicationContext), Mockito 목을 담은
        // 빈을 refresh() 전에 끼워 넣을 수 있는 GenericWebApplicationContext를 대신 쓴다.
        GenericWebApplicationContext context = new GenericWebApplicationContext();
        context.setServletContext(new MockServletContext());

        AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(context);
        reader.register(
                MinimalTestConfig.class,
                SecurityConfig.class,
                LoginRequiredEntryPoint.class,
                GlobalExceptionHandler.class
        );
        context.registerBean("itemController", ItemController.class, () -> new ItemController(itemService));
        context.registerBean("sessionController", SessionController.class, SessionController::new);

        context.refresh();

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void 비로그인_스토어리소스요청_401_LOGIN_REQUIRED() throws Exception {
        mockMvc.perform(get("/stores/items"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));
    }

    @Test
    public void 인증된상태_스토어리소스요청_200_컨트롤러도달_principal주입() throws Exception {
        doReturn(PageResponse.of(Collections.<ItemListResponse>emptyList(), 1, 10, 0))
                .when(itemService)
                .getItemList(any(LoginContext.class), any());

        LoginContext loginContext = new LoginContext(100, 1, "tester", "LOCAL");

        mockMvc.perform(get("/stores/items")
                        .with(authentication(UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"content\":[]")));
    }

    @Test
    public void 비로그인_세션조회_401_LOGIN_REQUIRED_500아님() throws Exception {
        // /auth/session/me는 permitAll 경로라 필터가 막아주지 않는다. anonymous principal이
        // @AuthenticationPrincipal에서 타입 불일치로 조용히 null이 되고(에러 아님), 컨트롤러가
        // 직접 LOGIN_REQUIRED를 던지는지 — 500이 아니라 401인지 — 확인한다.
        mockMvc.perform(get("/auth/session/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));
    }

    @Test
    public void 로그아웃_이후_같은세션으로_보호API재요청시_401() throws Exception {
        doReturn(PageResponse.of(Collections.<ItemListResponse>emptyList(), 1, 10, 0))
                .when(itemService)
                .getItemList(any(LoginContext.class), any());

        LoginContext loginContext = new LoginContext(100, 1, "tester", "LOCAL");
        MockHttpSession session = new MockHttpSession();

        // 1) 인증된 상태로 보호 리소스 접근 -> 200 (SecurityContext가 세션에 저장됨)
        mockMvc.perform(get("/stores/items")
                        .session(session)
                        .with(authentication(UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of()))))
                .andExpect(status().isOk());

        // 2) 로그아웃 -> 204, LogoutFilter가 세션을 무효화한다
        mockMvc.perform(post("/auth/session/logout").session(session))
                .andExpect(status().isNoContent());

        // 3) 같은(무효화된) 세션으로 다시 요청 -> 401
        mockMvc.perform(get("/stores/items").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));
    }
}
