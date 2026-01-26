package com.jaegokeeper.ddan.user.utils;

import org.springframework.stereotype.Component;

@Component
public class TestVerifier implements SocialVerifier {
    @Override public String provider() { return "TEST"; }

    @Override
    public SocialProfile verify(String accessToken) {
        // accessToken을 providerUid로 쓰는 간단 버전
        if (accessToken == null || accessToken.isEmpty()) throw new IllegalArgumentException("token required");
        return new SocialProfile("test-" + accessToken, "테스트유저", "test@example.com");
    }
}