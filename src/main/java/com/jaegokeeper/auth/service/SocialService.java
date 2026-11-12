package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.PendingSocialSignup;
import com.jaegokeeper.auth.dto.SocialCompleteResponse;
import com.jaegokeeper.auth.dto.UidDTO;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.auth.utils.SocialVerifier;
import com.jaegokeeper.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.jaegokeeper.exception.ErrorCode.*;

@Slf4j
@Service
public class SocialService {

    private final UserAuthMapper userAuthMapper;
    private final PendingSocialSignupService pendingSocialSignupService;
    private final Map<String, SocialVerifier> verifiersByProvider;

    public SocialService(
            UserAuthMapper userAuthMapper,
            PendingSocialSignupService pendingSocialSignupService,
            List<SocialVerifier> verifiers
    ) {
        this.userAuthMapper = userAuthMapper;
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
        LoginTarget target = userAuthMapper.findByProviderUid(provider, profile.getProviderUid());

        if (target != null) {
            if (!Boolean.TRUE.equals(target.getIsActive())) throw new BusinessException(USER_NOT_ACTIVE);
            return SocialCompleteResponse.login(target.getUserId());
        }

        Integer linkedUserId = linkExistingUserByVerifiedEmail(provider, profile);
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

    private Integer linkExistingUserByVerifiedEmail(String provider, SocialProfile profile) {
        String email = profile.getEmail() != null ? profile.getEmail().trim().toLowerCase() : null;

        if (email == null || email.isEmpty() || !profile.isEmailVerified()) {
            return null;
        }

        UserDTO existing = userAuthMapper.findUserByEmail(email);
        if (existing == null) {
            return null;
        }

        return linkProviderToExistingUser(existing, provider, profile.getProviderUid());
    }

    private int linkProviderToExistingUser(UserDTO existing, String provider, String providerUid) {
        if (!Boolean.TRUE.equals(existing.getIsActive())) {
            throw new BusinessException(USER_NOT_ACTIVE);
        }

        UidDTO linked = new UidDTO();
        linked.setUserId(existing.getUserId());
        linked.setProvider(provider);
        linked.setProviderUid(providerUid);

        try {
            userAuthMapper.insertAuth(linked);
        } catch (DuplicateKeyException e) {
            UidDTO existingAuth = userAuthMapper.findAuthByUserAndProvider(existing.getUserId(), provider);
            if (existingAuth == null || !providerUid.equals(existingAuth.getProviderUid())) {
                throw new BusinessException(FORBIDDEN);
            }
        }

        return existing.getUserId();
    }
}
