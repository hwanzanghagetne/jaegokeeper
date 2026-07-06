package com.jaegokeeper.onboarding.service;

import com.jaegokeeper.auth.dto.PendingSocialSignup;
import com.jaegokeeper.auth.dto.UidDTO;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.service.PendingSocialSignupService;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.auth.utils.PasswordHasher;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.email.service.EmailAuthService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.onboarding.dto.OwnerSignUpRequest;
import com.jaegokeeper.onboarding.dto.OwnerSignUpRequest.AccountInfo;
import com.jaegokeeper.onboarding.dto.OwnerSignUpRequest.StoreInfo;
import com.jaegokeeper.onboarding.dto.OwnerSignUpResponse;
import com.jaegokeeper.onboarding.dto.SocialOwnerSignUpRequest;
import com.jaegokeeper.store.dto.StoreDto;
import com.jaegokeeper.store.mapper.StoreMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final EmailAuthService emailAuthService;
    private final UserAuthMapper userAuthMapper;
    private final StoreMapper storeMapper;
    private final PendingSocialSignupService pendingSocialSignupService;
    private final SessionService sessionService;

    @Transactional
    public OwnerSignUpResponse ownerSignUp(OwnerSignUpRequest req) {
        String email = req.getAccount().getEmail().trim().toLowerCase();

        emailAuthService.assertVerified(email);

        UserDTO user = createLocalUser(req.getAccount(), email);
        StoreDto store = createOwnerStore(user.getUserId(), req.getStore());

        return OwnerSignUpResponse.builder()
                .userId(user.getUserId())
                .storeId(store.getStoreId())
                .userName(user.getUserName())
                .storeName(store.getStoreName())
                .build();
    }

    @Transactional
    public OwnerSignUpResponse socialOwnerSignUp(SocialOwnerSignUpRequest req, HttpServletRequest request) {
        PendingSocialSignup pending = pendingSocialSignupService.consume(req.getSignupToken());
        String email = normalizeVerifiedSocialEmail(pending);

        UserDTO existing = userAuthMapper.findUserByEmail(email);
        if (existing != null) {
            int userId = linkProviderToExistingUser(existing, pending.getProvider(), pending.getProviderUid());
            StoreDto store = createOwnerStoreIfMissing(userId, req.getStore());
            sessionService.createSession(userId, pending.getProvider(), request);
            return OwnerSignUpResponse.builder()
                    .userId(userId)
                    .storeId(store.getStoreId())
                    .userName(existing.getUserName())
                    .storeName(store.getStoreName())
                    .build();
        }

        UserDTO user = createSocialUser(pending, req.getAccount(), email);
        StoreDto store = createSocialOwnerStore(user.getUserId(), req.getStore());
        sessionService.createSession(user.getUserId(), pending.getProvider(), request);

        return OwnerSignUpResponse.builder()
                .userId(user.getUserId())
                .storeId(store.getStoreId())
                .userName(user.getUserName())
                .storeName(store.getStoreName())
                .build();
    }

    @Transactional
    public Integer linkExistingUserByVerifiedEmail(String provider, SocialProfile profile) {
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

    private UserDTO createLocalUser(AccountInfo account, String email) {
        if (userAuthMapper.findUserByEmail(email) != null) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);
        }

        UserDTO user = UserDTO.builder()
                .userMail(email)
                .passHash(PasswordHasher.hash(account.getPassword()))
                .userName(account.getUserName())
                .userPhone(account.getUserPhone())
                .isActive(true)
                .emailVerified(true)
                .build();

        try {
            int inserted = userAuthMapper.insertUser(user);
            if (inserted != 1 || user.getUserId() == null) {
                throw new BusinessException(REGISTER_FAILED);
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("[OWNER_SIGNUP] 유저 생성 실패", e);
            throw new BusinessException(INTERNAL_ERROR, e);
        }

        UidDTO uid = new UidDTO();
        uid.setUserId(user.getUserId());
        uid.setProvider("LOCAL");
        uid.setProviderUid(email);
        userAuthMapper.insertAuth(uid);

        return user;
    }

    private UserDTO createSocialUser(
            PendingSocialSignup pending,
            SocialOwnerSignUpRequest.AccountInfo account,
            String email
    ) {
        UserDTO user = UserDTO.builder()
                .userMail(email)
                .passHash(null)
                .userName(account.getUserName())
                .userPhone(account.getUserPhone())
                .isActive(true)
                .emailVerified(true)
                .build();

        try {
            int inserted = userAuthMapper.insertUser(user);
            if (inserted != 1 || user.getUserId() == null) {
                throw new BusinessException(REGISTER_FAILED);
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("[SOCIAL_OWNER_SIGNUP] 유저 생성 실패 provider={}", pending.getProvider(), e);
            throw new BusinessException(INTERNAL_ERROR, e);
        }

        registerUid(user.getUserId(), pending.getProvider(), pending.getProviderUid());
        return user;
    }

    private StoreDto createOwnerStore(int userId, StoreInfo store) {
        StoreDto storeDto = StoreDto.builder()
                .userId(userId)
                .storeName(store.getStoreName())
                .storeTel(store.getStoreTel())
                .storeAdd1(store.getStoreAdd1())
                .storeAdd2(store.getStoreAdd2())
                .build();

        return insertStore(userId, storeDto, "OWNER_SIGNUP");
    }

    private StoreDto createSocialOwnerStore(int userId, SocialOwnerSignUpRequest.StoreInfo store) {
        StoreDto storeDto = StoreDto.builder()
                .userId(userId)
                .storeName(store.getStoreName())
                .storeTel(store.getStoreTel())
                .storeAdd1(store.getStoreAdd1())
                .storeAdd2(store.getStoreAdd2())
                .build();

        return insertStore(userId, storeDto, "SOCIAL_OWNER_SIGNUP");
    }

    private StoreDto createOwnerStoreIfMissing(int userId, SocialOwnerSignUpRequest.StoreInfo store) {
        StoreDto storeDto = StoreDto.builder()
                .userId(userId)
                .storeName(store.getStoreName())
                .storeTel(store.getStoreTel())
                .storeAdd1(store.getStoreAdd1())
                .storeAdd2(store.getStoreAdd2())
                .build();

        return insertStore(userId, storeDto, "SOCIAL_OWNER_SIGNUP_LINKED");
    }

    private StoreDto insertStore(int userId, StoreDto storeDto, String logPrefix) {
        try {
            int inserted = storeMapper.insertStore(storeDto);
            if (inserted != 1 || storeDto.getStoreId() == 0) {
                throw new BusinessException(INTERNAL_ERROR);
            }
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("[{}] 스토어 생성 실패 userId={}", logPrefix, userId, e);
            throw new BusinessException(INTERNAL_ERROR, e);
        }

        return storeDto;
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
        } catch (org.springframework.dao.DuplicateKeyException e) {
            UidDTO existingAuth = userAuthMapper.findAuthByUserAndProvider(existing.getUserId(), provider);
            if (existingAuth == null || !providerUid.equals(existingAuth.getProviderUid())) {
                throw new BusinessException(FORBIDDEN);
            }
        }

        return existing.getUserId();
    }

    private String normalizeVerifiedSocialEmail(PendingSocialSignup pending) {
        String email = pending.getEmail() != null ? pending.getEmail().trim().toLowerCase() : null;
        if (email == null || email.isEmpty() || !pending.isEmailVerified()) {
            throw new BusinessException(EMAIL_NOT_VERIFIED);
        }
        return email;
    }

    private void registerUid(int userId, String provider, String providerUid) {
        UidDTO uid = new UidDTO();
        uid.setUserId(userId);
        uid.setProvider(provider);
        uid.setProviderUid(providerUid);

        try {
            int inserted = userAuthMapper.insertAuth(uid);
            if (inserted != 1) {
                throw new BusinessException(INTERNAL_ERROR);
            }
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("[SOCIAL_SIGNUP] uid 생성 실패 userId={}, provider={}", userId, provider, e);
            throw new BusinessException(INTERNAL_ERROR, e);
        }
    }
}
