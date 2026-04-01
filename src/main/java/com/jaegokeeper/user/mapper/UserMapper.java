package com.jaegokeeper.user.mapper;

import com.jaegokeeper.user.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    void updateUser(UserDto userDto);

    int countByEmail(@Param("email") String email);
}
