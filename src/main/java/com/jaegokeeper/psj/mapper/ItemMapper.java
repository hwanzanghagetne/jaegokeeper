package com.jaegokeeper.psj.mapper;

import com.jaegokeeper.psj.dto.ItemDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItemMapper {

    void insertItem(ItemDto dto);

    boolean existsByName(String itemName);

    ItemDto selectItemById(int itemId);

    boolean existsByStoreId(int storeId);

    // 전체조회
    List<ItemDto> selectAllItems();
}
