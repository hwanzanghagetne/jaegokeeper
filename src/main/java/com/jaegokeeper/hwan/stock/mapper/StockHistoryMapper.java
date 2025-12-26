package com.jaegokeeper.hwan.stock.mapper;

import com.jaegokeeper.hwan.stock.enums.StockHistoryType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockHistoryMapper {

//    @Param quantity는 변화량
    int insertHistory(@Param("stockId") Integer stockId,
                      @Param("hisType") StockHistoryType hisType,
                      @Param("delta") Integer quantity);
}
