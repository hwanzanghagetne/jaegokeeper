package com.jaegokeeper.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.auth.utils.GoogleVerifier;
import com.jaegokeeper.auth.utils.KakaoVerifier;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.auth.utils.SocialVerifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SocialVerifierTest {

    @Mock
    private RestTemplate restTemplate;

    private GoogleVerifier google;
    private KakaoVerifier kakao;

    @Before
    public void setUp() {
        google = new GoogleVerifier(restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(google, "clientId", "my-client-id");
        kakao = new KakaoVerifier(restTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(kakao, "appId", "12345");
    }

    @Test
    public void 구글_aud가_일치하면_프로필을_반환() throws Exception {
        stubGoogle("{\"aud\":\"my-client-id\"}");

        SocialProfile profile = google.verify("token");

        assertEquals("sub-1", profile.getProviderUid());
        assertTrue(profile.isEmailVerified());
    }

    @Test
    public void 구글_aud가_다르면_예외() throws Exception {
        stubGoogle("{\"aud\":\"other-app\"}");

        assertRejected(google);
    }

    @Test
    public void 구글_clientId_미설정이면_예외() throws Exception {
        ReflectionTestUtils.setField(google, "clientId", "");
        stubGoogle("{\"aud\":\"\"}");

        assertRejected(google);
    }

    @Test
    public void 카카오_app_id가_일치하면_프로필을_반환() throws Exception {
        stubKakao("{\"app_id\":12345}");

        SocialProfile profile = kakao.verify("token");

        assertEquals("777", profile.getProviderUid());
    }

    @Test
    public void 카카오_app_id가_다르면_예외() throws Exception {
        stubKakao("{\"app_id\":99999}");

        assertRejected(kakao);
    }

    private void stubGoogle(String tokeninfoBody) {
        when(restTemplate.getForObject(startsWith("https://oauth2.googleapis.com/tokeninfo"), eq(String.class), eq("token")))
                .thenReturn(tokeninfoBody);
        lenient().when(restTemplate.exchange(eq("https://www.googleapis.com/oauth2/v3/userinfo"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"sub\":\"sub-1\",\"name\":\"n\",\"email\":\"a@b.com\",\"email_verified\":true}"));
    }

    private void stubKakao(String tokenInfoBody) {
        when(restTemplate.exchange(eq("https://kapi.kakao.com/v1/user/access_token_info"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(tokenInfoBody));
        lenient().when(restTemplate.exchange(eq("https://kapi.kakao.com/v2/user/me"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"id\":777}"));
    }

    private void assertRejected(SocialVerifier verifier) {
        assertThrows(IllegalArgumentException.class, () -> verifier.verify("token"));
    }
}
