package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.auth.utils.SocialVerifier;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.onboarding.service.OnboardingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
public class SocialService {

    private final UserAuthMapper userAuthMapper;
    private final OnboardingService onboardingService;
    private final Map<String, SocialVerifier> verifiersByProvider;

    public SocialService(
            UserAuthMapper userAuthMapper,
            OnboardingService onboardingService,
            List<SocialVerifier> verifiers
    ) {
        this.userAuthMapper = userAuthMapper;
        this.onboardingService = onboardingService;

        Map<String, SocialVerifier> m = new HashMap<>();
        for (SocialVerifier v : verifiers) m.put(v.provider(), v);
        this.verifiersByProvider = Collections.unmodifiableMap(m);
    }

    @Transactional
    public int complete(String provider, String accessToken) {
        SocialVerifier verifier = verifiersByProvider.get(provider);
        if (verifier == null) throw new BusinessException(BAD_REQUEST);
        if (accessToken == null || accessToken.isEmpty()) throw new BusinessException(BAD_REQUEST);

        SocialProfile profile;
        try {
            profile = verifier.verify(accessToken);
        } catch (Exception e) {
            throw new BusinessException(BAD_REQUEST);
        }

        return findOrRegister(provider, profile);
    }

    private int findOrRegister(String provider, SocialProfile profile) {
        LoginTarget tgt = userAuthMapper.findByProviderUid(provider, profile.getProviderUid());

        if (tgt != null) {
            if (!tgt.getIsActive()) throw new BusinessException(USER_NOT_ACTIVE);
            return tgt.getUserId();
        }

        return onboardingService.socialSignUp(provider, profile);
    }
}
