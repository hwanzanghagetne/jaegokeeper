package com.jaegokeeper.auth.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class NaverVerifier implements SocialVerifier {

    private final RestTemplate restTemplate;
    private final ObjectMapper om;

    @Override public String provider() { return "NAVER"; }

    @Override
    public SocialProfile verify(String accessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<String> res = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET, new HttpEntity<>(headers), String.class
        );

        JsonNode n = om.readTree(res.getBody());
        JsonNode resp = n.get("response");
        if (resp == null) throw new IllegalArgumentException("naver response missing");

        String id    = resp.has("id")    ? resp.get("id").asText()    : null;
        String name  = resp.has("name")  ? resp.get("name").asText()  : null;
        String email = resp.has("email") ? resp.get("email").asText() : null;
        boolean emailVerified = false; // Naver 프로필 API에 검증 플래그가 없어 자동 이메일 연동에서 제외

        if (id == null) throw new IllegalArgumentException("naver id missing");
        return new SocialProfile(id, name, email, emailVerified);
    }
}
