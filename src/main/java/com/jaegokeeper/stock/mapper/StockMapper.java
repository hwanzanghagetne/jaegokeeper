package com.jaegokeeper.stock.mapper;

import com.jaegokeeper.stock.domain.Stock;
import com.jaegokeeper.stock.dto.StockDetailResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockMapper {

    int insertStock(Stock stock);

    Integer findStockAmountByItemId(@Param("storeId") Integer storeId,
                                    @Param("itemId") Integer itemId);

    int updateStockAmount(@Param("storeId") Integer storeId,
                          @Param("itemId") Integer itemId,
                          @Param("amount") Integer amount);

    int increaseQuantity(@Param("storeId") Integer storeId,
                         @Param("itemId") Integer itemId,
                         @Param("amount") Integer amount);

    int decreaseQuantity(@Param("storeId") Integer storeId,
                         @Param("itemId") Integer itemId,
                         @Param("amount") Integer amount);

    StockDetailResponse findStockDetail(@Param("storeId") Integer storeId,
                                        @Param("itemId") Integer itemId);
}
