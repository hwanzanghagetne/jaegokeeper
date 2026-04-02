package com.jaegokeeper.store.service;

import com.jaegokeeper.store.dto.StoreDto;
import com.jaegokeeper.store.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreMapper storeMapper;

    @Transactional
    public void updateStore(StoreDto storeDto) {
        storeMapper.updateStore(storeDto);
    }
}
