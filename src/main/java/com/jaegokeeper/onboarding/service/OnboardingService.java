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
        AccountInfo account = req.getAccount();
        StoreInfo store = req.getStore();
        String email = account.getEmail().trim().toLowerCase();

        // 1. 이메일 인증 완료 여부 확인
        emailAuthService.assertVerified(email);

        // 2. 이메일 중복 확인
        if (userAuthMapper.findUserByEmail(email) != null) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);
        }

        // 3. 유저 생성
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

        // 4. 로컬 로그인 수단 등록 (uid row 생성)
        UidDTO uid = new UidDTO();
        uid.setUserId(user.getUserId());
        uid.setProvider("LOCAL");
        uid.setProviderUid(email);
        userAuthMapper.insertAuth(uid);

        // 5. 스토어 생성
        StoreDto storeDto = StoreDto.builder()
                .userId(user.getUserId())
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
            log.error("[OWNER_SIGNUP] 스토어 생성 실패 userId={}", user.getUserId(), e);
            throw new BusinessException(INTERNAL_ERROR);
        }

        return OwnerSignUpResponse.builder()
                .userId(user.getUserId())
                .storeId(storeDto.getStoreId())
                .userName(user.getUserName())
                .storeName(storeDto.getStoreName())
                .build();
    }

    @Transactional
    public int socialSignUp(String provider, SocialProfile profile) {
        String email = profile.getEmail() != null ? profile.getEmail().trim().toLowerCase() : null;

        // 정책: 이메일이 있고 provider에서 검증된 경우에만 기존 유저와 자동 연동
        if (email != null && !email.isEmpty() && profile.isEmailVerified()) {
            UserDTO existing = userAuthMapper.findUserByEmail(email);
            if (existing != null) {
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
                        // 같은 이메일이라도 provider 계정 연결 정보가 다르면 자동 연동하지 않음
                        throw new BusinessException(FORBIDDEN);
                    }
                }
                return existing.getUserId();
            }
        }

        if (email == null || email.isEmpty()) {
            // user.user_mail NOT NULL 스키마와 정책을 맞춰 명시적으로 실패시킴
            throw new BusinessException(BAD_REQUEST);
        }

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

        StoreDto store = new StoreDto();
        store.setUserId(user.getUserId());
        store.setStoreName(user.getUserName() + "의 스토어");
        try {
            int inserted = storeMapper.insertStore(store);
            if (inserted != 1 || store.getStoreId() == 0) {
                throw new BusinessException(INTERNAL_ERROR);
            }
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("[SOCIAL_SIGNUP] 스토어 생성 실패 userId={}", user.getUserId(), e);
            throw new BusinessException(INTERNAL_ERROR);
        }

        UidDTO uid = new UidDTO();
        uid.setUserId(user.getUserId());
        uid.setProvider(provider);
        uid.setProviderUid(profile.getProviderUid());
        try {
            int inserted = userAuthMapper.insertAuth(uid);
            if (inserted != 1) {
                throw new BusinessException(INTERNAL_ERROR);
            }
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("[SOCIAL_SIGNUP] uid 생성 실패 userId={}, provider={}", user.getUserId(), provider, e);
            throw new BusinessException(INTERNAL_ERROR);
        }

        return user.getUserId();
    }
}
