package com.jaegokeeper.auth.utils;

public interface SocialVerifier {
    SocialProfile verify(String accessToken) throws Exception;
    String provider(); // "GOOGLE"/"KAKAO"/"NAVER"
}

