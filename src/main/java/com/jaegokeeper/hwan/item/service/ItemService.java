package com.jaegokeeper.hwan.item.service;

import com.jaegokeeper.hwan.item.dto.*;

import java.util.List;

public interface ItemService {

    //아이템 생성
    int createItem(ItemCreateRequestDTO itemCreateRequestDTO);

    //아이템 삭제
    void softDeleteItem(Integer storeId, Integer itemId);

    //아이템 전체조회
    PageResponseDTO<ItemListDTO> getItemList(Integer storeId, int page, int size, List<String> filters, String keyword);

    //아이템 상세
    ItemDetailDTO getItemDetail(Integer storeId, Integer itemId);


    //아이템 수정 (상세페이지용)
    void modifyItem(Integer storeId, Integer itemId, ItemModifyRequestDTO dto);



}
