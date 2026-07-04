package com.jaegokeeper.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialCompleteResponse {
    private final boolean signupRequired;
    private final Integer userId;
    private final String signupToken;
    private final String provider;
    private final String email;
    private final String displayName;

    public static SocialCompleteResponse login(int userId) {
        return SocialCompleteResponse.builder()
                .signupRequired(false)
                .userId(userId)
                .build();
    }

    public static SocialCompleteResponse signupRequired(
            String signupToken,
            String provider,
            String email,
            String displayName
    ) {
        return SocialCompleteResponse.builder()
                .signupRequired(true)
                .signupToken(signupToken)
                .provider(provider)
                .email(email)
                .displayName(displayName)
                .build();
    }
}
