package com.jaegokeeper.auth.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Integer userId;

    private String userName;
    private String userMail;
    private String userPhone;
    private Boolean isActive;

    private String passHash;
    private Boolean emailVerified;

}
