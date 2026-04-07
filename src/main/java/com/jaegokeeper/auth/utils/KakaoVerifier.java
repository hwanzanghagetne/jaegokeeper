package com.jaegokeeper.auth.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KakaoVerifier implements SocialVerifier {
    private final ObjectMapper om = new ObjectMapper();
    @Override public String provider() { return "KAKAO"; }

    @Override
    public SocialProfile verify(String accessToken) throws Exception {
        String json = HttpJson.get("https://kapi.kakao.com/v2/user/me", accessToken);
        JsonNode n = om.readTree(json);

        String id = n.has("id") ? n.get("id").asText() : null;
        String nickname = null;
        String email = null;
        boolean emailVerified = false;

        JsonNode kakaoAccount = n.get("kakao_account");
        if (kakaoAccount != null) {
            JsonNode profile = kakaoAccount.get("profile");
            if (profile != null && profile.has("nickname")) nickname = profile.get("nickname").asText();
            if (kakaoAccount.has("email")) email = kakaoAccount.get("email").asText();
            if (kakaoAccount.has("is_email_verified")) {
                emailVerified = kakaoAccount.get("is_email_verified").asBoolean(false);
            }
        }

        if (id == null) throw new IllegalArgumentException("kakao id missing");
        return new SocialProfile(id, nickname, email, emailVerified);
    }
}
