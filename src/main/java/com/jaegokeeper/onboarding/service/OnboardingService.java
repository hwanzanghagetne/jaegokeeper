package com.jaegokeeper.onboarding.service;

import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.utils.PasswordHasher;
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

        // 1. 이메일 인증 완료 여부 확인
        emailAuthService.assertVerified(account.getEmail());

        // 2. 이메일 중복 확인
        if (userAuthMapper.findUserByEmail(account.getEmail()) != null) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);
        }

        // 3. 유저 생성
        UserDTO user = UserDTO.builder()
                .userMail(account.getEmail())
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

        // 4. 스토어 생성
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
}
