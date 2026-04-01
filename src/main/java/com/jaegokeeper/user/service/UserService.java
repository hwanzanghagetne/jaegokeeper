package com.jaegokeeper.user.service;

import com.jaegokeeper.user.dto.UserDto;
import com.jaegokeeper.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserMapper userMapper;
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Transactional
    public void updateUser(UserDto userDto) {
        userMapper.updateUser(userDto);
    }
}
