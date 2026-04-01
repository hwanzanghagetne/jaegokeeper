package com.jaegokeeper.store.service;

import com.jaegokeeper.store.dto.StoreDto;
import com.jaegokeeper.store.mapper.StoreMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {

    private final StoreMapper storeMapper;
    public StoreService(StoreMapper storeMapper) {
        this.storeMapper = storeMapper;
    }

    @Transactional
    public void updateStore(StoreDto storeDto) {
        storeMapper.updateStore(storeDto);
    }
}
