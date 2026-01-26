package com.jaegokeeper.ddan.user.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleVerifier implements SocialVerifier {
    private final ObjectMapper om = new ObjectMapper();

    @Override public String provider() { return "GOOGLE"; }

    @Override
    public SocialProfile verify(String accessToken) throws Exception {
        // Google userinfo (access_token으로 조회)
        String json = HttpJson.get("https://www.googleapis.com/oauth2/v3/userinfo", accessToken);
        JsonNode n = om.readTree(json);

        String sub = text(n, "sub");           // providerUid로 사용
        String name = text(n, "name");
        String email = text(n, "email");
        if (sub == null) throw new IllegalArgumentException("google userinfo sub missing");

        return new SocialProfile(sub, name, email);
    }

    private String text(JsonNode n, String k) {
        return (n != null && n.has(k) && !n.get(k).isNull()) ? n.get(k).asText() : null;
    }
}
