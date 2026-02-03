package com.jaegokeeper.hwan.item.service;

import com.jaegokeeper.hwan.item.dto.request.ItemCreateRequest;
import com.jaegokeeper.hwan.item.dto.request.ItemPageRequest;
import com.jaegokeeper.hwan.item.dto.request.ItemUpdateRequest;
import com.jaegokeeper.hwan.item.dto.response.ItemDetailResponse;
import com.jaegokeeper.hwan.item.dto.response.ItemListResponse;
import com.jaegokeeper.hwan.item.dto.response.ItemPageResponse;

public interface ItemService {

    //아이템 생성
    Integer createItem(Integer storeId, ItemCreateRequest itemCreateRequest);

    //아이템 삭제
    void softDeleteItem(Integer storeId, Integer itemId);

    //아이템 전체조회
    ItemPageResponse<ItemListResponse> getItemList(Integer storeId, ItemPageRequest dto);

    //아이템 상세
    ItemDetailResponse getItemDetail(Integer storeId, Integer itemId);

    //아이템 수정 (상세페이지용)
    void updateItem(Integer storeId, Integer itemId, ItemUpdateRequest dto);

    void toggleItemPin(Integer storeId, Integer itemId);


}
