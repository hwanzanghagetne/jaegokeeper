package com.jaegokeeper.psj.service;

import com.jaegokeeper.psj.dto.ItemDto2;
import com.jaegokeeper.psj.mapper.AlbaMapper;
import com.jaegokeeper.psj.mapper.ItemMapper2;
import com.jaegokeeper.psj.mapper.StoreMapper2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService2 {
    private final StoreMapper2 storeMapper;
    private final ItemMapper2 itemMapper;
    private final AlbaMapper albaMapper;

    public ItemService2(StoreMapper2 storeMapper, ItemMapper2 itemMapper, AlbaMapper albaMapper) {
        this.storeMapper = storeMapper;
        this.itemMapper = itemMapper;
        this.albaMapper = albaMapper;
    }

    // 저장 메서드
    public void saveDto(ItemDto2 dto) {
        // Store 존재 확인
        if(!albaMapper.existsByStoreId(dto.getStoreId())) {
            throw new IllegalArgumentException("store가 존재하지 않습니다.");
        }


        // 아이템 이름 중복 체크
        if(itemMapper.existsByName(dto.getItemName())) {
            throw new IllegalArgumentException("존재하는 아이템 이름입니다.");
        }

        // 저장
        itemMapper.insertItem(dto);
    }

    // 전체 조회 메서드 (saveDto 밖에 독립적으로)
    public List<ItemDto2> getAllItems() {
        return itemMapper.selectAllItems();
    }
}