package com.jaegokeeper.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserDetailResponse {

    private Integer userId;
    private String userName;
    private String userMail;
    private String userPhone;
    private List<String> providers;
}
