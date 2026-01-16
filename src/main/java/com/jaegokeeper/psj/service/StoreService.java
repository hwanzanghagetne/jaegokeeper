package com.jaegokeeper.psj.service;

import com.jaegokeeper.psj.dto.StoreDto;
import com.jaegokeeper.psj.mapper.StoreMapper2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {

    private final StoreMapper2 storeMapper2;
    public StoreService(StoreMapper2 storeMapper2) {
        this.storeMapper2 = storeMapper2;
    }

    // store 정보 수정
    @Transactional
    public void updateStore(StoreDto storeDto) {
        storeMapper2.updateStore(storeDto);
    }
}
