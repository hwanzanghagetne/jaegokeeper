package com.jaegokeeper.ddan.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {

    private int userId;

    private String userName;
//    private String userPass;
    private String userMail;
    private String userPhone;
//    private String login_type;
    private Boolean isActive;

    private String userRole;
    private String userPassChk;

}
