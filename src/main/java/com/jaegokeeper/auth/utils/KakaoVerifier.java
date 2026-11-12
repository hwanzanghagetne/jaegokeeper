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
public class KakaoVerifier implements SocialVerifier {

    private final RestTemplate restTemplate;
    private final ObjectMapper om;

    @Value("${KAKAO_APP_ID:}")
    private String appId;

    @Override
    public String provider() {
        return Provider.KAKAO.name();
    }

    @Override
    public SocialProfile verify(String accessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        String tokenAppId = om.readTree(restTemplate.exchange(
                "https://kapi.kakao.com/v1/user/access_token_info",
                HttpMethod.GET, new HttpEntity<>(headers), String.class
        ).getBody()).path("app_id").asText(null);
        if (appId.isBlank() || !appId.equals(tokenAppId)) {
            throw new IllegalArgumentException("kakao token app_id mismatch");
        }

        JsonNode user = om.readTree(restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET, new HttpEntity<>(headers), String.class
        ).getBody());

        String id = user.path("id").asText(null);
        if (id == null) throw new IllegalArgumentException("kakao id missing");

        JsonNode account = user.path("kakao_account");
        return new SocialProfile(
                id,
                account.path("profile").path("nickname").asText(null),
                account.path("email").asText(null),
                account.path("is_email_verified").asBoolean(false)
        );
    }
}
