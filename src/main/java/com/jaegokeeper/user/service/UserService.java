package com.jaegokeeper.user.service;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.user.dto.UserDetailResponse;
import com.jaegokeeper.user.dto.UserUpdateRequest;
import com.jaegokeeper.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;

    @Transactional
    public void updateUser(LoginContext login, int userId, UserUpdateRequest dto) {
        if (login.getUserId() != userId) {
            throw new BusinessException(FORBIDDEN);
        }
        if (!userMapper.existsById(userId)) {
            throw new BusinessException(USER_NOT_FOUND);
        }
        dto.setUserId(userId);
        int updated = userMapper.updateUser(dto);
        if (updated == 0) {
            throw new BusinessException(STATE_CONFLICT);
        }
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(LoginContext login, int userId) {
        if (login.getUserId() != userId) {
            throw new BusinessException(FORBIDDEN);
        }
        UserDetailResponse detail = userMapper.findUserDetail(userId);
        if (detail == null) {
            throw new BusinessException(USER_NOT_FOUND);
        }
        detail.setProviders(userAuthMapper.findProvidersByUserId(userId));
        return detail;
    }
}
