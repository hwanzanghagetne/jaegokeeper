package com.jaegokeeper.auth.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Integer userId;

    private String userName;
//    private String userPass;
    private String userMail;
    private String userPhone;
//    private String login_type;
    private Boolean isActive;

    private String userRole;
    private String passHash;
    private String passChk;
    private Boolean emailVerified;

}
