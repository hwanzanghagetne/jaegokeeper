package com.jaegokeeper.auth;

import com.jaegokeeper.auth.dto.LoginRequest;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
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

    private LoginRequest request(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    private UserDTO user(int userId, boolean active, String password) {
        UserDTO user = new UserDTO();
        user.setUserId(userId);
        user.setIsActive(active);
        user.setPassHash(PasswordHasher.hash(password));
        return user;
    }

    @Test
    public void 로그인_성공시_유저ID_반환_이메일은_소문자로_조회() {
        when(userAuthMapper.findUserByEmail("test@example.com")).thenReturn(user(7, true, "password123"));

        int userId = localService.login(request(" Test@Example.com ", "password123"));

        assertEquals(7, userId);
    }

    @Test
    public void 로그인_존재하지않는_이메일_예외() {
        when(userAuthMapper.findUserByEmail("notexist@example.com")).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> localService.login(request("notexist@example.com", "password123")));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, e.getErrorCode());
    }

    @Test
    public void 로그인_비밀번호불일치_예외() {
        when(userAuthMapper.findUserByEmail("test@example.com")).thenReturn(user(7, true, "correctpassword"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> localService.login(request("test@example.com", "wrongpassword")));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, e.getErrorCode());
    }

    @Test
    public void 로그인_소셜_전용_계정은_비밀번호로_로그인할_수_없다() {
        UserDTO socialOnly = new UserDTO();
        socialOnly.setUserId(7);
        socialOnly.setIsActive(true);
        when(userAuthMapper.findUserByEmail("test@example.com")).thenReturn(socialOnly);

        BusinessException e = assertThrows(BusinessException.class,
                () -> localService.login(request("test@example.com", "password123")));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, e.getErrorCode());
    }

    @Test
    public void 로그인_비활성계정은_비밀번호가_맞을_때만_비활성_예외() {
        when(userAuthMapper.findUserByEmail("test@example.com")).thenReturn(user(7, false, "password123"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> localService.login(request("test@example.com", "password123")));

        assertEquals(ErrorCode.USER_NOT_ACTIVE, e.getErrorCode());
    }

    @Test
    public void 로그인_비활성계정이라도_비밀번호가_틀리면_계정_상태를_알려주지_않는다() {
        when(userAuthMapper.findUserByEmail("test@example.com")).thenReturn(user(7, false, "password123"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> localService.login(request("test@example.com", "wrongpassword")));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, e.getErrorCode());
    }
}
