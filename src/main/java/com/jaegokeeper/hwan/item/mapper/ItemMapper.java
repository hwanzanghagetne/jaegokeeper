package com.jaegokeeper.hwan.item.mapper;

import com.jaegokeeper.hwan.item.domain.Item;
import com.jaegokeeper.hwan.item.dto.response.ItemDetailResponse;
import com.jaegokeeper.hwan.item.dto.response.ItemListResponse;
import com.jaegokeeper.hwan.item.enums.ItemFilter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper {

    //아이템 등록
    int insertItem(Item item);

    //아이템 삭제
    int softDeleteItem(@Param("storeId") Integer storeId,
                       @Param("itemId") Integer itemId);

    int countItemList(@Param("storeId") Integer storeId,
                       @Param("filter") ItemFilter filter,
                       @Param("keyword")String keyword,
                      @Param("excludeZero")Boolean excludeZero);

    List<ItemListResponse> findItemList(@Param("storeId") Integer storeId,
                                        @Param("filter") ItemFilter filter,
                                        @Param("keyword") String keyword,
                                        @Param("excludeZero")Boolean excludeZero,
                                        @Param("offset") Integer offset,
                                        @Param("size") Integer size);

    //아이템 상세
    ItemDetailResponse findItemDetail(@Param("storeId") Integer storeId,
                                      @Param("itemId") Integer itemId);

    //아이템 수정
    int updateItem(@Param("storeId") Integer storeId,
                   @Param("itemId") Integer itemId,
                   @Param("itemName") String itemName,
                   @Param("isPinned") Boolean isPinned,
                   @Param("imageId") Integer imageId);

    int countByStoreIdAndItemId(@Param("storeId") Integer storeId,
                                @Param("itemId") Integer itemId);
}
