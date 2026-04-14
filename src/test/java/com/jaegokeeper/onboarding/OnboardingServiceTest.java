package com.jaegokeeper.onboarding;

import com.jaegokeeper.auth.dto.UidDTO;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.email.service.EmailAuthService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.onboarding.service.OnboardingService;
import com.jaegokeeper.store.mapper.StoreMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

    @Test
    public void 소셜가입_이메일없음_예외() {
        SocialProfile profile = new SocialProfile("provider-uid", "tester", null, false);

        try {
            onboardingService.socialSignUp("GOOGLE", profile);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
        }
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
            onboardingService.socialSignUp(provider, profile);
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

        int userId = onboardingService.socialSignUp(provider, profile);
        assertEquals(1, userId);
    }
}
