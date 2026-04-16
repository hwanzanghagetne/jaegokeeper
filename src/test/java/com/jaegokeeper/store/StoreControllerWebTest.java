package com.jaegokeeper.store;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.utils.LoginUserArgumentResolver;
import com.jaegokeeper.auth.utils.SessionInterceptor;
import com.jaegokeeper.exception.GlobalExceptionHandler;
import com.jaegokeeper.store.controller.StoreController;
import com.jaegokeeper.store.service.StoreService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class StoreControllerWebTest {

    @Mock
    private StoreService storeService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        StoreController controller = new StoreController(storeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new SessionInterceptor())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .build();
    }

    @Test
    public void 점포수정_매장명공백_400() throws Exception {
        MockHttpSession session = loginSession(1);
        String body = "{\"storeName\":\"\",\"storeAdd1\":\"서울\",\"storeAdd2\":\"101호\",\"storeTel\":\"02-0000-0000\"}";

        mockMvc.perform(put("/stores/1")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        verifyNoInteractions(storeService);
    }

    @Test
    public void 점포수정_로그인없음_401() throws Exception {
        String body = "{\"storeName\":\"자초단\",\"storeAdd1\":\"서울\",\"storeAdd2\":\"101호\",\"storeTel\":\"02-0000-0000\"}";

        mockMvc.perform(put("/stores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("\"code\":\"LOGIN_REQUIRED\"")));

        verifyNoInteractions(storeService);
    }

    @Test
    public void 점포수정_정상요청_204() throws Exception {
        MockHttpSession session = loginSession(1);
        String body = "{\"storeName\":\"자초단\",\"storeAdd1\":\"서울\",\"storeAdd2\":\"101호\",\"storeTel\":\"02-0000-0000\"}";

        mockMvc.perform(put("/stores/1")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        verify(storeService).updateStore(any(LoginContext.class), eq(1), any());
    }

    private MockHttpSession loginSession(int storeId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("login", new LoginContext(100, storeId, "tester", "LOCAL"));
        return session;
    }
}

