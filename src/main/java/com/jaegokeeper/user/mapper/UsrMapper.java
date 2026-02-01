package com.jaegokeeper.user.mapper;

import com.jaegokeeper.psj.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UsrMapper {

    int countByEmail(@Param("email") String email);

}
