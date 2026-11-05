package com.jaegokeeper.onboarding;

import com.jaegokeeper.auth.dto.PendingSocialSignup;
import com.jaegokeeper.auth.dto.UidDTO;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.service.PendingSocialSignupService;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.email.service.EmailAuthService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.onboarding.dto.OwnerSignUpRequest;
import com.jaegokeeper.onboarding.dto.OwnerSignUpResponse;
import com.jaegokeeper.onboarding.dto.SocialOwnerSignUpRequest;
import com.jaegokeeper.onboarding.dto.StoreInfo;
import com.jaegokeeper.onboarding.service.OnboardingService;
import com.jaegokeeper.store.dto.StoreDto;
import com.jaegokeeper.store.mapper.StoreMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OnboardingServiceTest {

    @InjectMocks
    private OnboardingService onboardingService;

    @Mock
    private EmailAuthService emailAuthService;

    @Mock
    private UserAuthMapper userAuthMapper;

    @Mock
    private StoreMapper storeMapper;

    @Mock
    private PendingSocialSignupService pendingSocialSignupService;

    @Mock
    private SessionService sessionService;

    @Test
    public void 일반가입_성공() {
        OwnerSignUpRequest req = ownerSignUpRequest("Owner@Test.com");
        stubInserts();

        OwnerSignUpResponse res = onboardingService.ownerSignUp(req);

        verify(emailAuthService).assertVerified("owner@test.com");
        assertEquals(Integer.valueOf(10), res.getUserId());
        assertEquals(Integer.valueOf(20), res.getStoreId());
        verify(userAuthMapper).insertAuth(any(UidDTO.class));
    }

    @Test
    public void 일반가입_이미가입된이메일_예외() {
        when(userAuthMapper.findUserByEmail("owner@test.com")).thenReturn(new UserDTO());

        try {
            onboardingService.ownerSignUp(ownerSignUpRequest("owner@test.com"));
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, e.getErrorCode());
        }
        verify(storeMapper, never()).insertStore(any());
    }

    @Test
    public void 소셜가입_성공시_세션생성() {
        PendingSocialSignup pending = pendingSignup("Social@Test.com", true);
        when(pendingSocialSignupService.consume("token")).thenReturn(pending);
        stubInserts();

        OwnerSignUpResponse res = onboardingService.socialOwnerSignUp(socialSignUpRequest(), null, null);

        assertEquals(Integer.valueOf(10), res.getUserId());
        assertEquals(Integer.valueOf(20), res.getStoreId());
        verify(sessionService).createSession(10, "GOOGLE", null, null);
    }

    @Test
    public void 소셜가입_이메일미인증_예외() {
        when(pendingSocialSignupService.consume("token")).thenReturn(pendingSignup("social@test.com", false));

        try {
            onboardingService.socialOwnerSignUp(socialSignUpRequest(), null, null);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, e.getErrorCode());
        }
        verify(userAuthMapper, never()).insertUser(any());
    }

    @Test
    public void 소셜가입_이미가입된이메일_예외() {
        when(pendingSocialSignupService.consume("token")).thenReturn(pendingSignup("social@test.com", true));
        when(userAuthMapper.findUserByEmail("social@test.com")).thenReturn(new UserDTO());

        try {
            onboardingService.socialOwnerSignUp(socialSignUpRequest(), null, null);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, e.getErrorCode());
        }
        verify(storeMapper, never()).insertStore(any());
        verify(sessionService, never()).createSession(anyInt(), any(), any(), any());
    }

    @Test
    public void 소셜연동_이메일없으면_null반환() {
        SocialProfile profile = new SocialProfile("provider-uid", "tester", null, false);

        Integer userId = onboardingService.linkExistingUserByVerifiedEmail("GOOGLE", profile);

        assertNull(userId);
    }

    @Test
    public void 소셜연동_기존연동과_providerUid불일치_예외() {
        String provider = "GOOGLE";
        SocialProfile profile = new SocialProfile("new-uid", "tester", "test@example.com", true);

        UserDTO existing = new UserDTO();
        existing.setUserId(1);
        existing.setIsActive(true);
        when(userAuthMapper.findUserByEmail(eq("test@example.com"))).thenReturn(existing);

        doThrow(new DuplicateKeyException("dup"))
                .when(userAuthMapper)
                .insertAuth(any(UidDTO.class));

        UidDTO linked = new UidDTO();
        linked.setUserId(1);
        linked.setProvider(provider);
        linked.setProviderUid("old-uid");
        when(userAuthMapper.findAuthByUserAndProvider(1, provider)).thenReturn(linked);

        try {
            onboardingService.linkExistingUserByVerifiedEmail(provider, profile);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
        }
    }

    @Test
    public void 소셜연동_기존동일연동이면_성공() {
        String provider = "GOOGLE";
        SocialProfile profile = new SocialProfile("same-uid", "tester", "test@example.com", true);

        UserDTO existing = new UserDTO();
        existing.setUserId(1);
        existing.setIsActive(true);
        when(userAuthMapper.findUserByEmail(eq("test@example.com"))).thenReturn(existing);

        doThrow(new DuplicateKeyException("dup"))
                .when(userAuthMapper)
                .insertAuth(any(UidDTO.class));

        UidDTO linked = new UidDTO();
        linked.setUserId(1);
        linked.setProvider(provider);
        linked.setProviderUid("same-uid");
        when(userAuthMapper.findAuthByUserAndProvider(1, provider)).thenReturn(linked);

        Integer userId = onboardingService.linkExistingUserByVerifiedEmail(provider, profile);
        assertEquals(Integer.valueOf(1), userId);
    }

    private void stubInserts() {
        when(userAuthMapper.insertUser(any(UserDTO.class))).thenAnswer(inv -> {
            ((UserDTO) inv.getArgument(0)).setUserId(10);
            return 1;
        });
        when(userAuthMapper.insertAuth(any(UidDTO.class))).thenReturn(1);
        when(storeMapper.insertStore(any(StoreDto.class))).thenAnswer(inv -> {
            ((StoreDto) inv.getArgument(0)).setStoreId(20);
            return 1;
        });
    }

    private OwnerSignUpRequest ownerSignUpRequest(String email) {
        OwnerSignUpRequest.AccountInfo account = new OwnerSignUpRequest.AccountInfo();
        ReflectionTestUtils.setField(account, "email", email);
        ReflectionTestUtils.setField(account, "password", "password123");
        ReflectionTestUtils.setField(account, "userName", "사장");
        OwnerSignUpRequest req = new OwnerSignUpRequest();
        ReflectionTestUtils.setField(req, "account", account);
        ReflectionTestUtils.setField(req, "store", storeInfo());
        return req;
    }

    private SocialOwnerSignUpRequest socialSignUpRequest() {
        SocialOwnerSignUpRequest.AccountInfo account = new SocialOwnerSignUpRequest.AccountInfo();
        ReflectionTestUtils.setField(account, "userName", "사장");
        SocialOwnerSignUpRequest req = new SocialOwnerSignUpRequest();
        ReflectionTestUtils.setField(req, "signupToken", "token");
        ReflectionTestUtils.setField(req, "account", account);
        ReflectionTestUtils.setField(req, "store", storeInfo());
        return req;
    }

    private StoreInfo storeInfo() {
        StoreInfo store = new StoreInfo();
        ReflectionTestUtils.setField(store, "storeName", "테스트점");
        return store;
    }

    private PendingSocialSignup pendingSignup(String email, boolean emailVerified) {
        SocialProfile profile = new SocialProfile("uid-1", "tester", email, emailVerified);
        return new PendingSocialSignup("token", "GOOGLE", profile, LocalDateTime.now().plusMinutes(10));
    }
}
