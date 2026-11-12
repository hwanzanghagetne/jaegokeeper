package com.jaegokeeper.auth;

import com.jaegokeeper.auth.dto.PendingSocialSignup;
import com.jaegokeeper.auth.service.PendingSocialSignupService;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.common.DateTimeConstants;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PendingSocialSignupServiceTest {

    private PendingSocialSignupService service;
    private final SocialProfile profile = new SocialProfile("uid-1", "tester", "tester@example.com", true);

    @Before
    public void setUp() {
        service = new PendingSocialSignupService();
    }

    @SuppressWarnings("unchecked")
    private Map<String, PendingSocialSignup> store() {
        return (Map<String, PendingSocialSignup>) ReflectionTestUtils.getField(service, "pendingSignups");
    }

    private void putExpired(String token) {
        store().put(token, new PendingSocialSignup(token, "GOOGLE", profile, LocalDateTime.now(DateTimeConstants.KST).minusMinutes(1)));
    }

    @Test
    public void 생성한_토큰으로_가입_정보를_한번_꺼낼_수_있음() {
        PendingSocialSignup created = service.create("GOOGLE", profile);

        PendingSocialSignup consumed = service.consume(created.getToken());

        assertEquals("GOOGLE", consumed.getProvider());
        assertEquals("uid-1", consumed.getProviderUid());
        assertEquals("tester@example.com", consumed.getEmail());
        assertNotNull(consumed.getToken());
    }

    @Test
    public void 같은_토큰은_두번_사용할_수_없음() {
        PendingSocialSignup created = service.create("GOOGLE", profile);
        service.consume(created.getToken());

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.consume(created.getToken()));

        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
    }

    @Test
    public void 없는_토큰_빈_토큰_null은_거부() {
        assertEquals(ErrorCode.BAD_REQUEST,
                assertThrows(BusinessException.class, () -> service.consume("unknown")).getErrorCode());
        assertEquals(ErrorCode.BAD_REQUEST,
                assertThrows(BusinessException.class, () -> service.consume(" ")).getErrorCode());
        assertEquals(ErrorCode.BAD_REQUEST,
                assertThrows(BusinessException.class, () -> service.consume(null)).getErrorCode());
    }

    @Test
    public void 만료된_토큰은_거부하고_다시_쓸_수_없음() {
        putExpired("expired-token");

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.consume("expired-token"));

        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
        assertFalse(store().containsKey("expired-token"));
    }

    @Test
    public void 새로_만들_때_만료된_토큰은_정리() {
        putExpired("old-token");

        PendingSocialSignup created = service.create("KAKAO", profile);

        assertFalse(store().containsKey("old-token"));
        assertTrue(store().containsKey(created.getToken()));
    }
}
