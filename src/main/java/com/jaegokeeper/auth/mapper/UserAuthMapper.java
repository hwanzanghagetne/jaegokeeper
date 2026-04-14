package com.jaegokeeper.auth.mapper;

import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.UidDTO;
import com.jaegokeeper.auth.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAuthMapper {

    LoginTarget findByProviderUid(
            @Param("provider") String provider,
            @Param("providerUid") String providerUid
    );
    LoginTarget findByUserIdForSession(int userId);

    int insertUser(UserDTO userDTO);

    int insertAuth(UidDTO uidDTO);

    UserDTO findUserByEmail(@Param("email") String email);

    UidDTO findAuthByUserAndProvider(
            @Param("userId") int userId,
            @Param("provider") String provider
    );

}
