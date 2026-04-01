package com.jaegokeeper.item.mapper;

import com.jaegokeeper.item.domain.Item;
import com.jaegokeeper.item.dto.ItemUpdateParamImg;
import com.jaegokeeper.item.dto.response.ItemDetailResponse;
import com.jaegokeeper.item.dto.response.ItemListResponse;
import com.jaegokeeper.item.enums.ItemFilter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper {

    int insertItem(Item item);

    int softDeleteItem(@Param("storeId") Integer storeId,
                       @Param("itemId") Integer itemId);

    int countItemList(@Param("storeId") Integer storeId,
                      @Param("filter") ItemFilter filter,
                      @Param("keyword") String keyword,
                      @Param("excludeZero") Boolean excludeZero);

    List<ItemListResponse> findItemList(@Param("storeId") Integer storeId,
                                        @Param("filter") ItemFilter filter,
                                        @Param("keyword") String keyword,
                                        @Param("excludeZero") Boolean excludeZero,
                                        @Param("offset") Integer offset,
                                        @Param("size") Integer size);

    ItemDetailResponse findItemDetail(@Param("storeId") Integer storeId,
                                      @Param("itemId") Integer itemId);

    int updateItem(@Param("storeId") Integer storeId,
                   @Param("itemId") Integer itemId,
                   @Param("dto") ItemUpdateParamImg dto);

    int countByStoreIdAndItemId(@Param("storeId") Integer storeId,
                                @Param("itemId") Integer itemId);

    int togglePin(@Param("storeId") Integer storeId,
                  @Param("itemId") Integer itemId);
}
