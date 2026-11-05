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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
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
        // refresh() 전에 목 빈을 등록하려고 GenericWebApplicationContext를 쓴다.
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

        mockMvc.perform(get("/stores/items")
                        .session(session)
                        .with(authentication(UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of()))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/session/logout").session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/stores/items").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));
    }

    @Test
    public void GET_로그아웃은_세션을_무효화하지_않는다() throws Exception {
        LoginContext loginContext = new LoginContext(100, 1, "tester", "LOCAL");
        MockHttpSession session = new MockHttpSession();
        RequestPostProcessor login =
                authentication(UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of()));

        mockMvc.perform(get("/stores/items").session(session).with(login)).andExpect(status().isOk());

        mockMvc.perform(get("/auth/session/logout").session(session));

        mockMvc.perform(get("/stores/items").session(session)).andExpect(status().isOk());
    }
}
