package com.jaegokeeper.psj.mapper;

import com.jaegokeeper.psj.dto.ItemDto2;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItemMapper2 {

    void insertItem(ItemDto2 dto);

    boolean existsByName(String itemName);

    ItemDto2 selectItemById(int itemId);

    boolean existsByStoreId(int storeId);

    // 전체조회
    List<ItemDto2> selectAllItems();
}
