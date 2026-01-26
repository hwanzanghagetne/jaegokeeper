package com.jaegokeeper.ddan.user.mapper;

import com.jaegokeeper.ddan.user.dto.LoginTarget;
import com.jaegokeeper.ddan.user.dto.TicketDTO;
import com.jaegokeeper.ddan.user.dto.UidDTO;
import com.jaegokeeper.ddan.user.dto.UserDTO;
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

    int insertTicket(TicketDTO ticketDTO);
    TicketDTO findValidTicket(String ticketKey);
    int markUsed(String ticketKey);

}