package com.jaegokeeper.user;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.user.dto.UserUpdateRequest;
import com.jaegokeeper.user.mapper.UserMapper;
import com.jaegokeeper.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
}
