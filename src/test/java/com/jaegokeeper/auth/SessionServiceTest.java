package com.jaegokeeper.auth;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.SessionResponse;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionServiceTest {

    @InjectMocks
    private SessionService sessionService;

    @Mock
    private UserAuthMapper userAuthMapper;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void 세션생성_존재하지않는유저_예외() {
        when(userAuthMapper.findByUserIdForSession(1)).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            sessionService.createSession(1, "LOCAL", request, response);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void 세션생성_성공시_기존세션무효화후_새세션발급() {
        LoginTarget target = new LoginTarget();
        target.setUserId(1);
        target.setStoreId(10);
        target.setUserName("tester");
        when(userAuthMapper.findByUserIdForSession(1)).thenReturn(target);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession oldSession = new MockHttpSession();
        request.setSession(oldSession);
        MockHttpServletResponse response = new MockHttpServletResponse();

        sessionService.createSession(1, "LOCAL", request, response);

        assertTrue("기존 세션은 무효화돼야 한다", oldSession.isInvalid());
        // MockHttpServletRequest는 getSession(true) 호출 시 무효화된 세션을 감지하면
        // 새 MockHttpSession을 만들어준다(실제 서블릿 컨테이너의 세션 재생성과 동일한 동작).
        assertNotSame("새 세션이 발급돼야 한다", oldSession, request.getSession(false));
    }

    @Test
    public void 세션생성_성공시_SecurityContext에_LoginContext저장() {
        LoginTarget target = new LoginTarget();
        target.setUserId(1);
        target.setStoreId(10);
        target.setUserName("tester");
        when(userAuthMapper.findByUserIdForSession(1)).thenReturn(target);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        SessionResponse result = sessionService.createSession(1, "LOCAL", request, response);

        ArgumentCaptor<SecurityContext> captor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(securityContextRepository).saveContext(captor.capture(), eq(request), eq(response));

        Object principal = captor.getValue().getAuthentication().getPrincipal();
        assertTrue(principal instanceof LoginContext);

        LoginContext login = (LoginContext) principal;
        assertEquals(1, login.getUserId());
        assertEquals(10, login.getStoreId());
        assertEquals("tester", login.getUserName());
        assertEquals("LOCAL", login.getProvider());

        assertEquals(Integer.valueOf(1), result.getUserId());
        assertEquals(Integer.valueOf(10), result.getStoreId());
        assertEquals("tester", result.getUserName());
    }
}
