package com.jaegokeeper.auth.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NaverVerifier implements SocialVerifier {
    private final ObjectMapper om = new ObjectMapper();
    @Override public String provider() { return "NAVER"; }

    @Override
    public SocialProfile verify(String accessToken) throws Exception {
        String json = HttpJson.get("https://openapi.naver.com/v1/nid/me", accessToken);
        JsonNode n = om.readTree(json);

        JsonNode resp = n.get("response");
        if (resp == null) throw new IllegalArgumentException("naver response missing");

        String id = resp.has("id") ? resp.get("id").asText() : null;
        String name = resp.has("name") ? resp.get("name").asText() : null;
        String email = resp.has("email") ? resp.get("email").asText() : null;

        if (id == null) throw new IllegalArgumentException("naver id missing");
        return new SocialProfile(id, name, email);
    }
}