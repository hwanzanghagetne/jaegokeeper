package com.jaegokeeper.auth.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {
    private Integer userId;
    private Integer storeId;
    private String userMail;
    private String userName;
    private String redirectUrl;
    private String provider;
}
