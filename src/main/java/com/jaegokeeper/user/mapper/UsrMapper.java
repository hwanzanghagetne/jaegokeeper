package com.jaegokeeper.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UsrMapper {

    int countByEmail(@Param("email") String email);
}
