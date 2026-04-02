package com.jaegokeeper.user.mapper;

import com.jaegokeeper.user.dto.UserUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    void updateUser(UserUpdateRequest userUpdateRequest);

    int countByEmail(@Param("email") String email);
}
