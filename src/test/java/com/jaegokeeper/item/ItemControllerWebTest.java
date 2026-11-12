package com.jaegokeeper.item;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import com.jaegokeeper.item.controller.ItemController;
import com.jaegokeeper.item.dto.response.ItemListResponse;
import com.jaegokeeper.item.service.ItemService;
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

@RunWith(MockitoJUnitRunner.class)
public class ItemControllerWebTest {

    @Mock
    private ItemService itemService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        ItemController controller = new ItemController(itemService);
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
    public void 스토어리소스_로그인시_200() throws Exception {
        doReturn(PageResponse.of(Collections.<ItemListResponse>emptyList(), 1, 10, 0))
                .when(itemService)
                .getItemList(any(LoginContext.class), any());

        login(1);

        mockMvc.perform(get("/stores/items"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"content\":[]")));
    }

    private static void login(int storeId) {
        LoginContext loginContext = new LoginContext(100, storeId, "tester", "LOCAL");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of()));
    }
}
