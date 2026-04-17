package com.jaegokeeper.onboarding.service;

import com.jaegokeeper.auth.dto.UidDTO;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.utils.PasswordHasher;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.email.service.EmailAuthService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.onboarding.dto.OwnerSignUpRequest;
import com.jaegokeeper.onboarding.dto.OwnerSignUpRequest.AccountInfo;
import com.jaegokeeper.onboarding.dto.OwnerSignUpRequest.StoreInfo;
import com.jaegokeeper.onboarding.dto.OwnerSignUpResponse;
import com.jaegokeeper.store.dto.StoreDto;
import com.jaegokeeper.store.mapper.StoreMapper;
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
    public int socialSignUp(String provider, SocialProfile profile) {
        String email = profile.getEmail() != null ? profile.getEmail().trim().toLowerCase() : null;

        if (email != null && !email.isEmpty() && profile.isEmailVerified()) {
            UserDTO existing = userAuthMapper.findUserByEmail(email);
            if (existing != null) {
                return linkProviderToExistingUser(existing, provider, profile);
            }
        }

        if (email == null || email.isEmpty()) {
            throw new BusinessException(BAD_REQUEST);
        }

        return createNewSocialUser(provider, profile, email);
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
            throw new BusinessException(INTERNAL_ERROR);
        }

        UidDTO uid = new UidDTO();
        uid.setUserId(user.getUserId());
        uid.setProvider("LOCAL");
        uid.setProviderUid(email);
        userAuthMapper.insertAuth(uid);

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

        try {
            int inserted = storeMapper.insertStore(storeDto);
            if (inserted != 1 || storeDto.getStoreId() == 0) {
                throw new BusinessException(INTERNAL_ERROR);
            }
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("[OWNER_SIGNUP] 스토어 생성 실패 userId={}", userId, e);
            throw new BusinessException(INTERNAL_ERROR);
        }

        return storeDto;
    }

    private int linkProviderToExistingUser(UserDTO existing, String provider, SocialProfile profile) {
        if (!Boolean.TRUE.equals(existing.getIsActive())) {
            throw new BusinessException(USER_NOT_ACTIVE);
        }

        UidDTO linked = new UidDTO();
        linked.setUserId(existing.getUserId());
        linked.setProvider(provider);
        linked.setProviderUid(profile.getProviderUid());

        try {
            userAuthMapper.insertAuth(linked);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            UidDTO existingAuth = userAuthMapper.findAuthByUserAndProvider(existing.getUserId(), provider);
            if (existingAuth == null || !profile.getProviderUid().equals(existingAuth.getProviderUid())) {
                throw new BusinessException(FORBIDDEN);
            }
        }

        return existing.getUserId();
    }

    private int createNewSocialUser(String provider, SocialProfile profile, String email) {
        UserDTO user = new UserDTO();
        String displayName = profile.getDisplayName();
        user.setUserName(displayName != null && !displayName.trim().isEmpty() ? displayName.trim() : provider + "_USER");
        user.setUserMail(email);
        user.setIsActive(true);
        user.setEmailVerified(profile.isEmailVerified());

        try {
            int inserted = userAuthMapper.insertUser(user);
            if (inserted != 1 || user.getUserId() == null) {
                throw new BusinessException(REGISTER_FAILED);
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("[SOCIAL_SIGNUP] 유저 생성 실패 provider={}", provider, e);
            throw new BusinessException(INTERNAL_ERROR);
        }

        createSocialStore(user.getUserId(), user.getUserName());
        registerUid(user.getUserId(), provider, profile.getProviderUid());

        return user.getUserId();
    }

    private void createSocialStore(int userId, String userName) {
        StoreDto store = new StoreDto();
        store.setUserId(userId);
        store.setStoreName(userName + "의 스토어");

        try {
            int inserted = storeMapper.insertStore(store);
            if (inserted != 1 || store.getStoreId() == 0) {
                throw new BusinessException(INTERNAL_ERROR);
            }
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("[SOCIAL_SIGNUP] 스토어 생성 실패 userId={}", userId, e);
            throw new BusinessException(INTERNAL_ERROR);
        }
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
            throw new BusinessException(INTERNAL_ERROR);
        }
    }
}
