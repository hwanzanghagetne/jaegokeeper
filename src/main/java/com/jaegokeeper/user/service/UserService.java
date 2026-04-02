package com.jaegokeeper.user.service;

import com.jaegokeeper.user.dto.UserUpdateRequest;
import com.jaegokeeper.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    @Transactional
    public void updateUser(UserUpdateRequest userUpdateRequest) {
        userMapper.updateUser(userUpdateRequest);
    }
}
