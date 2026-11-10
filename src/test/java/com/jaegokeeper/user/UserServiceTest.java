package com.jaegokeeper.user;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.email.service.EmailAuthService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.user.dto.UserDetailResponse;
import com.jaegokeeper.user.dto.UserUpdateRequest;
import com.jaegokeeper.user.mapper.UserMapper;
import com.jaegokeeper.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserAuthMapper userAuthMapper;

    @Mock
    private EmailAuthService emailAuthService;

    private final LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");

    private UserDetailResponse currentUser() {
        UserDetailResponse detail = new UserDetailResponse();
        detail.setUserId(10);
        detail.setUserMail("tester@example.com");
        return detail;
    }

    @Test
    public void 유저수정_0건영향_예외() {
        UserUpdateRequest req = new UserUpdateRequest();

        when(userMapper.findUserDetail(10)).thenReturn(currentUser());
        when(userMapper.updateUser(any(UserUpdateRequest.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.updateUser(login, 10, req));

        assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
    }

    @Test
    public void 유저수정_본인아님_예외() {
        UserUpdateRequest req = new UserUpdateRequest();

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.updateUser(login, 99, req));

        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
    }

    @Test
    public void 유저수정_대상없음_예외() {
        UserUpdateRequest req = new UserUpdateRequest();

        when(userMapper.findUserDetail(10)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.updateUser(login, 10, req));

        assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 이메일_변경은_인증된_새_이메일이면_소문자로_저장() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setUserMail("New@Example.com");

        when(userMapper.findUserDetail(10)).thenReturn(currentUser());
        when(userMapper.countByEmail("new@example.com")).thenReturn(0);
        when(userMapper.updateUser(req)).thenReturn(1);

        userService.updateUser(login, 10, req);

        verify(emailAuthService).assertVerified("new@example.com");
        assertEquals("new@example.com", req.getUserMail());
    }

    @Test
    public void 이메일_변경은_인증되지_않았으면_예외() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setUserMail("new@example.com");

        when(userMapper.findUserDetail(10)).thenReturn(currentUser());
        when(userMapper.countByEmail("new@example.com")).thenReturn(0);
        doThrow(new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED))
                .when(emailAuthService).assertVerified("new@example.com");

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.updateUser(login, 10, req));

        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, e.getErrorCode());
        verify(userMapper, never()).updateUser(any(UserUpdateRequest.class));
    }

    @Test
    public void 이메일_변경은_이미_가입된_이메일이면_예외() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setUserMail("taken@example.com");

        when(userMapper.findUserDetail(10)).thenReturn(currentUser());
        when(userMapper.countByEmail("taken@example.com")).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.updateUser(login, 10, req));

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, e.getErrorCode());
        verify(emailAuthService, never()).assertVerified(any());
    }

    @Test
    public void 현재와_같은_이메일이면_인증없이_이메일은_변경하지_않음() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setUserMail("Tester@Example.com");
        req.setUserPhone("010-0000-0000");

        when(userMapper.findUserDetail(10)).thenReturn(currentUser());
        when(userMapper.updateUser(req)).thenReturn(1);

        userService.updateUser(login, 10, req);

        verify(emailAuthService, never()).assertVerified(any());
        assertNull(req.getUserMail());
    }

    @Test
    public void 유저조회_정상_연동수단포함() {
        UserDetailResponse detail = new UserDetailResponse();
        detail.setUserId(10);
        detail.setUserName("tester");
        detail.setUserMail("tester@example.com");
        detail.setUserPhone("010-0000-0000");

        when(userMapper.findUserDetail(10)).thenReturn(detail);
        when(userAuthMapper.findProvidersByUserId(10)).thenReturn(List.of("LOCAL", "KAKAO"));

        UserDetailResponse result = userService.getUserDetail(login, 10);

        assertEquals("tester@example.com", result.getUserMail());
        assertEquals(List.of("LOCAL", "KAKAO"), result.getProviders());
    }

    @Test
    public void 유저조회_본인아님_예외() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.getUserDetail(login, 99));

        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
    }

    @Test
    public void 유저조회_대상없음_예외() {
        when(userMapper.findUserDetail(10)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.getUserDetail(login, 10));

        assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
    }
}
