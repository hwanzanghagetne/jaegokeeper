package com.jaegokeeper.ddan.user.utils;

public interface SocialVerifier {
    SocialProfile verify(String accessToken) throws Exception;
    String provider(); // "GOOGLE"/"KAKAO"/"NAVER"
}

