package com.jaegokeeper.onboarding.service;

import com.jaegokeeper.auth.dto.PendingSocialSignup;
import com.jaegokeeper.auth.dto.UidDTO;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.enums.Provider;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.service.PendingSocialSignupService;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.auth.utils.PasswordHasher;
import com.jaegokeeper.email.service.EmailAuthService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.onboarding.dto.OwnerSignUpRequest;
import com.jaegokeeper.onboarding.dto.OwnerSignUpResponse;
import com.jaegokeeper.onboarding.dto.SocialOwnerSignUpRequest;
import com.jaegokeeper.onboarding.dto.StoreInfo;
import com.jaegokeeper.store.dto.StoreDto;
import com.jaegokeeper.store.mapper.StoreMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
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
        OwnerSignUpRequest.AccountInfo account = req.getAccount();
        String email = account.getEmail().trim().toLowerCase();

        emailAuthService.assertVerified(email);

        UserDTO user = createUser(email, PasswordHasher.hash(account.getPassword()),
                account.getUserName(), account.getUserPhone(), Provider.LOCAL.name(), email);
        StoreDto store = createStore(user.getUserId(), req.getStore());

        return toResponse(user, store);
    }

    @Transactional
    public OwnerSignUpResponse socialOwnerSignUp(SocialOwnerSignUpRequest req, HttpServletRequest request, HttpServletResponse response) {
        PendingSocialSignup pending = pendingSocialSignupService.consume(req.getSignupToken());
        String email = normalizeVerifiedSocialEmail(pending);
        SocialOwnerSignUpRequest.AccountInfo account = req.getAccount();

        UserDTO user = createUser(email, null, account.getUserName(), account.getUserPhone(),
                pending.getProvider(), pending.getProviderUid());
        StoreDto store = createStore(user.getUserId(), req.getStore());
        sessionService.createSession(user.getUserId(), pending.getProvider(), request, response);

        return toResponse(user, store);
    }

    private UserDTO createUser(String email, String passHash, String userName, String userPhone,
                               String provider, String providerUid) {
        if (userAuthMapper.findUserByEmail(email) != null) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);
        }

        UserDTO user = UserDTO.builder()
                .userMail(email)
                .passHash(passHash)
                .userName(userName)
                .userPhone(userPhone)
                .isActive(true)
                .emailVerified(true)
                .build();

        try {
            int inserted = userAuthMapper.insertUser(user);
            if (inserted != 1 || user.getUserId() == null) {
                throw new BusinessException(REGISTER_FAILED);
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);
        } catch (DataAccessException e) {
            log.error("[SIGNUP] 유저 생성 실패 provider={}", provider, e);
            throw new BusinessException(INTERNAL_ERROR, e);
        }

        registerUid(user.getUserId(), provider, providerUid);
        return user;
    }

    private StoreDto createStore(int userId, StoreInfo store) {
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
        } catch (DataAccessException e) {
            log.error("[SIGNUP] 스토어 생성 실패 userId={}", userId, e);
            throw new BusinessException(INTERNAL_ERROR, e);
        }

        return storeDto;
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
        } catch (DataAccessException e) {
            log.error("[SIGNUP] uid 생성 실패 userId={}, provider={}", userId, provider, e);
            throw new BusinessException(INTERNAL_ERROR, e);
        }
    }

    private OwnerSignUpResponse toResponse(UserDTO user, StoreDto store) {
        return OwnerSignUpResponse.builder()
                .userId(user.getUserId())
                .storeId(store.getStoreId())
                .userName(user.getUserName())
                .storeName(store.getStoreName())
                .build();
    }
}
