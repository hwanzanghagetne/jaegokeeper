package com.jaegokeeper.hwan.stock.mapper;

import com.jaegokeeper.hwan.stock.domain.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockMapper {

    //아이템 생성시 stock 삽입용 추후 변경 가능??
    int insertStock(Stock stock);

    // 현재 재고수량 구하기
    Integer findQuantityByStockId(@Param("stockId") Integer stockId);

    //재고수량를 업데이트
    int updateQuantity(@Param("stockId") Integer stockId,
                       @Param("quantity") Integer quantity,
                       @Param("favoriteYn") Boolean favoriteYn);


    // 재고수량 업데이트(리스트에서)
    int increaseQuantity(@Param("stockId") Integer stockId,
                         @Param("amount") Integer amount);

    int decreaseQuantity(@Param("stockId") Integer stockId,
                         @Param("amount") Integer amount);

    //검증용
    Integer findStockIdByStoreAndItem(@Param("storeId") Integer storeId,
                                      @Param("itemId") Integer itemId);

    Integer countByStockIdAndStoreId(@Param("stockId") Integer stockId,
                                     @Param("storeId") Integer storeId);
}
