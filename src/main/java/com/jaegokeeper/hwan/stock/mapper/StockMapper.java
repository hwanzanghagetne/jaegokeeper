package com.jaegokeeper.hwan.stock.mapper;

import com.jaegokeeper.hwan.stock.domain.Stock;
import com.jaegokeeper.hwan.stock.dto.StockDetailResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockMapper {

    //아이템 생성시 stock 삽입용 추후 변경 가능??
    int insertStock(Stock stock);

    // 현재 재고수량 구하기
    Integer findStockAmountByItemId(@Param("storeId") Integer storeId,
                                    @Param("itemId") Integer itemId);


    //재고수량를 업데이트(재고, 버퍼 모두)
    int updateStockAmount(@Param("storeId") Integer storeId,
                          @Param("itemId") Integer itemId,
                          @Param("amount") Integer amount);


    // 재고수량 업데이트(리스트에서)
    int increaseQuantity(@Param("storeId") Integer storeId,
                         @Param("itemId") Integer itemId,
                         @Param("amount") Integer amount);

    int decreaseQuantity(@Param("storeId") Integer storeId,
                         @Param("itemId") Integer itemId,
                         @Param("amount") Integer amount);

    StockDetailResponse findStockDetail(@Param("storeId") Integer storeId,
                                        @Param("itemId") Integer itemId);

}
