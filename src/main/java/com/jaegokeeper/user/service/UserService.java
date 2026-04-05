package com.jaegokeeper.user.service;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.user.dto.UserUpdateRequest;
import com.jaegokeeper.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.exception.ErrorCode.FORBIDDEN;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    @Transactional
    public void updateUser(LoginContext login, int userId, UserUpdateRequest dto) {
        if (login.getUserId() != userId) {
            throw new BusinessException(FORBIDDEN);
        }
        dto.setUserId(userId);
        userMapper.updateUser(dto);
    }
}
