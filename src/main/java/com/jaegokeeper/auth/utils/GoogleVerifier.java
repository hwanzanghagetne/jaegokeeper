package com.jaegokeeper.auth.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleVerifier implements SocialVerifier {

    private final RestTemplate restTemplate;
    private final ObjectMapper om;

    @Override public String provider() { return "GOOGLE"; }

    @Override
    public SocialProfile verify(String accessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<String> res = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET, new HttpEntity<>(headers), String.class
        );

        JsonNode n = om.readTree(res.getBody());
        String sub   = text(n, "sub");
        String name  = text(n, "name");
        String email = text(n, "email");
        boolean emailVerified = bool(n, "email_verified");
        if (sub == null) throw new IllegalArgumentException("google userinfo sub missing");

        return new SocialProfile(sub, name, email, emailVerified);
    }

    private String text(JsonNode n, String k) {
        return (n != null && n.has(k) && !n.get(k).isNull()) ? n.get(k).asText() : null;
    }

    private boolean bool(JsonNode n, String k) {
        return n != null && n.has(k) && !n.get(k).isNull() && n.get(k).asBoolean(false);
    }
}
