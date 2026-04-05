package com.jaegokeeper.auth;

import com.jaegokeeper.auth.dto.LoginRequest;
import com.jaegokeeper.auth.dto.RegisterRequest;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.service.LocalService;
import com.jaegokeeper.auth.utils.PasswordHasher;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class LocalServiceTest {

    @InjectMocks
    private LocalService localService;

    @Mock
    private UserAuthMapper userAuthMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===================== register =====================

    @Test
    public void 회원가입_성공() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");
        req.setName("홍길동");

        when(userAuthMapper.findUserByEmail("test@example.com")).thenReturn(null);
        when(userAuthMapper.insertUser(any(UserDTO.class))).thenAnswer(inv -> {
            UserDTO dto = inv.getArgument(0);
            dto.setUserId(1);
            return 1;
        });

        var result = localService.register(req);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("홍길동", result.getName());
    }

    @Test
    public void 회원가입_이메일중복_예외() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("dup@example.com");
        req.setPassword("password123");
        req.setName("홍길동");

        when(userAuthMapper.findUserByEmail("dup@example.com")).thenReturn(new UserDTO());

        try {
            localService.register(req);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, e.getErrorCode());
        }
    }

    // ===================== loginAndIssueTicket =====================

    @Test
    public void 로그인_존재하지않는_이메일_예외() {
        LoginRequest req = new LoginRequest();
        req.setEmail("notexist@example.com");
        req.setPassword("password123");

        when(userAuthMapper.findUserByEmail("notexist@example.com")).thenReturn(null);

        try {
            localService.loginAndIssueTicket(req, null);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.INVALID_CREDENTIALS, e.getErrorCode());
        }
    }

    @Test
    public void 로그인_비밀번호불일치_예외() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrongpassword");

        UserDTO user = new UserDTO();
        user.setIsActive(true);
        user.setPassHash(PasswordHasher.hash("correctpassword"));

        when(userAuthMapper.findUserByEmail("test@example.com")).thenReturn(user);

        try {
            localService.loginAndIssueTicket(req, null);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.INVALID_CREDENTIALS, e.getErrorCode());
        }
    }

    @Test
    public void 로그인_비활성계정_예외() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        UserDTO user = new UserDTO();
        user.setIsActive(false);
        user.setPassHash(PasswordHasher.hash("password123"));

        when(userAuthMapper.findUserByEmail("test@example.com")).thenReturn(user);

        try {
            localService.loginAndIssueTicket(req, null);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.USER_NOT_ACTIVE, e.getErrorCode());
        }
    }
}
