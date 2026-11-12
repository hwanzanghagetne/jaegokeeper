package com.jaegokeeper.auth.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.auth.enums.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleVerifier implements SocialVerifier {

    private final RestTemplate restTemplate;
    private final ObjectMapper om;

    @Value("${GOOGLE_CLIENT_ID:}")
    private String clientId;

    @Override
    public String provider() {
        return Provider.GOOGLE.name();
    }

    @Override
    public SocialProfile verify(String accessToken) throws Exception {
        String aud = om.readTree(restTemplate.getForObject(
                "https://oauth2.googleapis.com/tokeninfo?access_token={token}", String.class, accessToken))
                .path("aud").asText(null);
        if (clientId.isBlank() || !clientId.equals(aud)) {
            throw new IllegalArgumentException("google token audience mismatch");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        JsonNode userInfo = om.readTree(restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET, new HttpEntity<>(headers), String.class
        ).getBody());

        String sub = userInfo.path("sub").asText(null);
        if (sub == null) throw new IllegalArgumentException("google userinfo sub missing");

        return new SocialProfile(
                sub,
                userInfo.path("name").asText(null),
                userInfo.path("email").asText(null),
                userInfo.path("email_verified").asBoolean(false)
        );
    }
}
