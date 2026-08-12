package com.jaegokeeper.store.service;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.store.dto.StoreDetailResponse;
import com.jaegokeeper.store.dto.StoreUpdateRequest;
import com.jaegokeeper.store.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreMapper storeMapper;

    @Transactional
    public void updateStore(LoginContext login, StoreUpdateRequest req) {
        int storeId = login.getStoreId();
        if (!storeMapper.existsById(storeId)) {
            throw new BusinessException(STORE_NOT_FOUND);
        }
        req.setStoreId(storeId);
        int updated = storeMapper.updateStore(req);
        if (updated == 0) {
            throw new BusinessException(STATE_CONFLICT);
        }
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse getStoreDetail(LoginContext login) {
        StoreDetailResponse detail = storeMapper.findStoreDetail(login.getStoreId());
        if (detail == null) {
            throw new BusinessException(STORE_NOT_FOUND);
        }
        return detail;
    }
}
