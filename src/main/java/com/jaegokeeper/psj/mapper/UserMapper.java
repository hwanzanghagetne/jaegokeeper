package com.jaegokeeper.psj.mapper;

import com.jaegokeeper.psj.dto.ScheduleRegisterDto;
import com.jaegokeeper.psj.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // user 정보 수정
    void updateUser(UserDto userDto);
}
