package com.jaegokeeper.common;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.utils.SessionInterceptor;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class StoreScopeInterceptorWebTest {

    @Mock
    private ItemService itemService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        ItemController controller = new ItemController(itemService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new SessionInterceptor())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void 스토어리소스_미로그인_401() throws Exception {
        mockMvc.perform(get("/stores/1/items"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));

        verifyNoInteractions(itemService);
    }

    @Test
    public void 스토어리소스_다른스토어_403() throws Exception {
        MockHttpSession session = loginSession(1);

        mockMvc.perform(get("/stores/2/items").session(session))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("\"code\":\"FORBIDDEN\"")));

        verifyNoInteractions(itemService);
    }

    @Test
    public void 스토어리소스_같은스토어_200() throws Exception {
        MockHttpSession session = loginSession(1);

        doReturn(PageResponse.of(Collections.<ItemListResponse>emptyList(), 1, 10, 0))
                .when(itemService)
                .getItemList(any(LoginContext.class), intThat(v -> v == 1), any());

        mockMvc.perform(get("/stores/1/items").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"content\":[]")));
    }

    private MockHttpSession loginSession(int storeId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("login", new LoginContext(100, storeId, "tester", "LOCAL"));
        return session;
    }
}
