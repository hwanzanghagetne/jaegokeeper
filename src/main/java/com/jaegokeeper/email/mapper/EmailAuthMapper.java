package com.jaegokeeper.email.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmailAuthMapper {

    int upsertAuthCode(@Param("email") String email,
                       @Param("code") String code);

    int verifyCode(@Param("email") String email,
                   @Param("code") String code);

    int isVerified(@Param("email") String email);
}
