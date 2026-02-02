package com.jaegokeeper.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginContext {
    private final int userId;
    private final int storeId;
    private final String userName;
    private final String provider; // LOCAL/GOOGLE/KAKAO/NAVER

    public LoginContext(int userId, int storeId, String userName, String provider) {
        this.userId = userId;
        this.storeId = storeId;
        this.userName = userName;
        this.provider = provider;
    }

}