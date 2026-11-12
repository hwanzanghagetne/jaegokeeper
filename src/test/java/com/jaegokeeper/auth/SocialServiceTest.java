package com.jaegokeeper.auth;

import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.PendingSocialSignup;
import com.jaegokeeper.auth.dto.SocialCompleteResponse;
import com.jaegokeeper.auth.dto.UidDTO;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.service.PendingSocialSignupService;
import com.jaegokeeper.auth.service.SocialService;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.auth.utils.SocialVerifier;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SocialServiceTest {

    @Mock
    private UserAuthMapper userAuthMapper;

    @Mock
    private PendingSocialSignupService pendingSocialSignupService;

    @Mock
    private SocialVerifier googleVerifier;

    private SocialService socialService;

    private final SocialProfile profile = new SocialProfile("uid-1", "tester", "tester@example.com", true);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(googleVerifier.provider()).thenReturn("GOOGLE");
        socialService = new SocialService(userAuthMapper, pendingSocialSignupService, List.of(googleVerifier));
    }

    private LoginTarget target(int userId, boolean active) {
        LoginTarget target = new LoginTarget();
        target.setUserId(userId);
        target.setIsActive(active);
        return target;
    }

    @Test
    public void 지원하지_않는_제공자면_예외() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> socialService.complete("NAVER", "token"));

        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
    }

    @Test
    public void 토큰이_비어_있으면_예외() {
        assertEquals(ErrorCode.BAD_REQUEST,
                assertThrows(BusinessException.class, () -> socialService.complete("GOOGLE", "")).getErrorCode());
        assertEquals(ErrorCode.BAD_REQUEST,
                assertThrows(BusinessException.class, () -> socialService.complete("GOOGLE", null)).getErrorCode());
    }

    @Test
    public void 소셜_검증에_실패하면_BAD_REQUEST로_변환() throws Exception {
        when(googleVerifier.verify("bad-token")).thenThrow(new IllegalArgumentException("audience mismatch"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> socialService.complete("GOOGLE", "bad-token"));

        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
        verify(userAuthMapper, never()).findByProviderUid(any(), any());
    }

    @Test
    public void 이미_연동된_활성_사용자는_로그인() throws Exception {
        when(googleVerifier.verify("token")).thenReturn(profile);
        when(userAuthMapper.findByProviderUid("GOOGLE", "uid-1")).thenReturn(target(10, true));

        SocialCompleteResponse response = socialService.complete("GOOGLE", "token");

        assertFalse(response.isSignupRequired());
        assertEquals(Integer.valueOf(10), response.getUserId());
    }

    @Test
    public void 연동된_사용자가_비활성이면_예외() throws Exception {
        when(googleVerifier.verify("token")).thenReturn(profile);
        when(userAuthMapper.findByProviderUid("GOOGLE", "uid-1")).thenReturn(target(10, false));

        BusinessException e = assertThrows(BusinessException.class,
                () -> socialService.complete("GOOGLE", "token"));

        assertEquals(ErrorCode.USER_NOT_ACTIVE, e.getErrorCode());
    }

    private UserDTO existingUser(int userId, boolean active) {
        UserDTO user = new UserDTO();
        user.setUserId(userId);
        user.setIsActive(active);
        return user;
    }

    private UidDTO linkedAuth(String providerUid) {
        UidDTO uid = new UidDTO();
        uid.setUserId(20);
        uid.setProvider("GOOGLE");
        uid.setProviderUid(providerUid);
        return uid;
    }

    @Test
    public void 연동은_없지만_인증된_이메일이_같은_계정이_있으면_연동하고_로그인() throws Exception {
        when(googleVerifier.verify("token")).thenReturn(profile);
        when(userAuthMapper.findByProviderUid("GOOGLE", "uid-1")).thenReturn(null);
        when(userAuthMapper.findUserByEmail("tester@example.com")).thenReturn(existingUser(20, true));

        SocialCompleteResponse response = socialService.complete("GOOGLE", "token");

        assertFalse(response.isSignupRequired());
        assertEquals(Integer.valueOf(20), response.getUserId());
        verify(userAuthMapper).insertAuth(any(UidDTO.class));
        verify(pendingSocialSignupService, never()).create(any(), any());
    }

    @Test
    public void 이메일이_인증되지_않은_소셜_계정은_기존_계정에_연동하지_않고_가입_필요() throws Exception {
        SocialProfile unverified = new SocialProfile("uid-1", "tester", "tester@example.com", false);
        when(googleVerifier.verify("token")).thenReturn(unverified);
        when(userAuthMapper.findByProviderUid("GOOGLE", "uid-1")).thenReturn(null);
        when(pendingSocialSignupService.create("GOOGLE", unverified))
                .thenReturn(new PendingSocialSignup("signup-token", "GOOGLE", unverified, LocalDateTime.now().plusMinutes(10)));

        SocialCompleteResponse response = socialService.complete("GOOGLE", "token");

        assertTrue(response.isSignupRequired());
        verify(userAuthMapper, never()).findUserByEmail(any());
        verify(userAuthMapper, never()).insertAuth(any());
    }

    @Test
    public void 연동하려는_기존_계정이_비활성이면_예외() throws Exception {
        when(googleVerifier.verify("token")).thenReturn(profile);
        when(userAuthMapper.findByProviderUid("GOOGLE", "uid-1")).thenReturn(null);
        when(userAuthMapper.findUserByEmail("tester@example.com")).thenReturn(existingUser(20, false));

        BusinessException e = assertThrows(BusinessException.class,
                () -> socialService.complete("GOOGLE", "token"));

        assertEquals(ErrorCode.USER_NOT_ACTIVE, e.getErrorCode());
        verify(userAuthMapper, never()).insertAuth(any());
    }

    @Test
    public void 이미_같은_소셜_계정이_연동되어_있으면_그대로_로그인() throws Exception {
        when(googleVerifier.verify("token")).thenReturn(profile);
        when(userAuthMapper.findByProviderUid("GOOGLE", "uid-1")).thenReturn(null);
        when(userAuthMapper.findUserByEmail("tester@example.com")).thenReturn(existingUser(20, true));
        doThrow(new DuplicateKeyException("dup")).when(userAuthMapper).insertAuth(any(UidDTO.class));
        when(userAuthMapper.findAuthByUserAndProvider(20, "GOOGLE")).thenReturn(linkedAuth("uid-1"));

        SocialCompleteResponse response = socialService.complete("GOOGLE", "token");

        assertEquals(Integer.valueOf(20), response.getUserId());
    }

    @Test
    public void 같은_이메일_계정에_다른_소셜_계정이_이미_연동되어_있으면_예외() throws Exception {
        when(googleVerifier.verify("token")).thenReturn(profile);
        when(userAuthMapper.findByProviderUid("GOOGLE", "uid-1")).thenReturn(null);
        when(userAuthMapper.findUserByEmail("tester@example.com")).thenReturn(existingUser(20, true));
        doThrow(new DuplicateKeyException("dup")).when(userAuthMapper).insertAuth(any(UidDTO.class));
        when(userAuthMapper.findAuthByUserAndProvider(20, "GOOGLE")).thenReturn(linkedAuth("other-uid"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> socialService.complete("GOOGLE", "token"));

        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
    }

    @Test
    public void 처음_오는_사용자는_가입_토큰과_함께_가입_필요_응답() throws Exception {
        when(googleVerifier.verify("token")).thenReturn(profile);
        when(userAuthMapper.findByProviderUid("GOOGLE", "uid-1")).thenReturn(null);
        when(userAuthMapper.findUserByEmail("tester@example.com")).thenReturn(null);
        when(pendingSocialSignupService.create("GOOGLE", profile))
                .thenReturn(new PendingSocialSignup("signup-token", "GOOGLE", profile, LocalDateTime.now().plusMinutes(10)));

        SocialCompleteResponse response = socialService.complete("GOOGLE", "token");

        assertTrue(response.isSignupRequired());
        assertEquals("signup-token", response.getSignupToken());
        assertEquals("GOOGLE", response.getProvider());
        assertEquals("tester@example.com", response.getEmail());
        assertEquals("tester", response.getDisplayName());
    }
}
