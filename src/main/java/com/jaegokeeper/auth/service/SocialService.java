package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.PendingSocialSignup;
import com.jaegokeeper.auth.dto.SocialCompleteResponse;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.auth.utils.SocialVerifier;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.onboarding.service.OnboardingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.jaegokeeper.exception.ErrorCode.*;

@Slf4j
@Service
public class SocialService {

    private final UserAuthMapper userAuthMapper;
    private final OnboardingService onboardingService;
    private final PendingSocialSignupService pendingSocialSignupService;
    private final Map<String, SocialVerifier> verifiersByProvider;

    public SocialService(
            UserAuthMapper userAuthMapper,
            OnboardingService onboardingService,
            PendingSocialSignupService pendingSocialSignupService,
            List<SocialVerifier> verifiers
    ) {
        this.userAuthMapper = userAuthMapper;
        this.onboardingService = onboardingService;
        this.pendingSocialSignupService = pendingSocialSignupService;

        Map<String, SocialVerifier> m = new HashMap<>();
        for (SocialVerifier v : verifiers) m.put(v.provider(), v);
        this.verifiersByProvider = Collections.unmodifiableMap(m);
    }

    @Transactional
    public SocialCompleteResponse complete(String provider, String accessToken) {
        SocialVerifier verifier = verifiersByProvider.get(provider);
        if (verifier == null) throw new BusinessException(BAD_REQUEST);
        if (accessToken == null || accessToken.isEmpty()) throw new BusinessException(BAD_REQUEST);

        SocialProfile profile;
        try {
            profile = verifier.verify(accessToken);
        } catch (Exception e) {
            log.warn("[SOCIAL_AUTH] 소셜 인증 검증 실패 provider={}", provider, e);
            throw new BusinessException(BAD_REQUEST, e);
        }

        return findOrPrepareSignup(provider, profile);
    }

    private SocialCompleteResponse findOrPrepareSignup(String provider, SocialProfile profile) {
        LoginTarget tgt = userAuthMapper.findByProviderUid(provider, profile.getProviderUid());

        if (tgt != null) {
            if (!tgt.getIsActive()) throw new BusinessException(USER_NOT_ACTIVE);
            return SocialCompleteResponse.login(tgt.getUserId());
        }

        Integer linkedUserId = onboardingService.linkExistingUserByVerifiedEmail(provider, profile);
        if (linkedUserId != null) {
            return SocialCompleteResponse.login(linkedUserId);
        }

        PendingSocialSignup pending = pendingSocialSignupService.create(provider, profile);
        return SocialCompleteResponse.signupRequired(
                pending.getToken(),
                provider,
                pending.getEmail(),
                pending.getDisplayName()
        );
    }
}
