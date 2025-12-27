package com.jaegokeeper.hwan.item.mapper;

import com.jaegokeeper.hwan.item.domain.Item;
import com.jaegokeeper.hwan.item.dto.ItemDetailDTO;
import com.jaegokeeper.hwan.item.dto.ItemListDTO;
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

    long countItemList(@Param("storeId") Integer storeId,
                       @Param("filters") List<String> filters,
                       @Param("keyword")String keyword);

    List<ItemListDTO> findItemList(@Param("storeId") Integer storeId,
                                   @Param("filters") List<String> filters,
                                   @Param("keyword") String keyword,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    //아이템 상세
    ItemDetailDTO findItemDetail(@Param("storeId") Integer storeId,
                                 @Param("itemId") Integer itemId);

    //아이템 수정
    int updateItem(@Param("storeId") Integer storeId,
                   @Param("itemId") Integer itemId,
                   @Param("itemName") String itemName,
                   @Param("imageId") Integer imageId);
}
