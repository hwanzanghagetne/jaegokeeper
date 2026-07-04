package com.jaegokeeper.user;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
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
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserAuthMapper userAuthMapper;

    @Test
    public void 유저수정_0건영향_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
        UserUpdateRequest req = new UserUpdateRequest();

        when(userMapper.existsById(10)).thenReturn(true);
        when(userMapper.updateUser(any(UserUpdateRequest.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.updateUser(login, 10, req));

        assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
    }

    @Test
    public void 유저수정_본인아님_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
        UserUpdateRequest req = new UserUpdateRequest();

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.updateUser(login, 99, req));

        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
    }

    @Test
    public void 유저수정_대상없음_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
        UserUpdateRequest req = new UserUpdateRequest();

        when(userMapper.existsById(10)).thenReturn(false);

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.updateUser(login, 10, req));

        assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 유저조회_정상_연동수단포함() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
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
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.getUserDetail(login, 99));

        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
    }

    @Test
    public void 유저조회_대상없음_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");

        when(userMapper.findUserDetail(10)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> userService.getUserDetail(login, 10));

        assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
    }
}
