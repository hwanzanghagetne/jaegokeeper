package com.jaegokeeper.psj.service;

import com.jaegokeeper.psj.dto.ItemDto;
import com.jaegokeeper.psj.mapper.AlbaMapper;
import com.jaegokeeper.psj.mapper.ItemMapper;
import com.jaegokeeper.psj.mapper.StoreMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    private final StoreMapper storeMapper;
    private final ItemMapper itemMapper;
    private final AlbaMapper albaMapper;

    public ItemService(StoreMapper storeMapper, ItemMapper itemMapper, AlbaMapper albaMapper) {
        this.storeMapper = storeMapper;
        this.itemMapper = itemMapper;
        this.albaMapper = albaMapper;
    }

    // 저장 메서드
    public void saveDto(ItemDto dto) {
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
    public List<ItemDto> getAllItems() {
        return itemMapper.selectAllItems();
    }
}