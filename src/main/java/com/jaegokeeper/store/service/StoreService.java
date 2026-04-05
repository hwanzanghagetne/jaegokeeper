package com.jaegokeeper.store.service;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.store.dto.StoreDto;
import com.jaegokeeper.store.dto.StoreUpdateRequest;
import com.jaegokeeper.store.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.exception.ErrorCode.FORBIDDEN;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreMapper storeMapper;

    @Transactional
    public void updateStore(LoginContext login, int storeId, StoreUpdateRequest req) {
        if (login.getStoreId() != storeId) {
            throw new BusinessException(FORBIDDEN);
        }
        req.setStoreId(storeId);
        storeMapper.updateStore(req);
    }
}
