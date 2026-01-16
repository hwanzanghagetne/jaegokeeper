package com.jaegokeeper.psj.service;

import com.jaegokeeper.psj.dto.UserDto;
import com.jaegokeeper.psj.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserMapper userMapper;
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // user 정보 수정
    @Transactional
    public  void updateUser(UserDto userDto) {
        userMapper.updateUser(userDto);
    }
}
