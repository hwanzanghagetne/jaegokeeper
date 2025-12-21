package com.jaegokeeper.hwan.mapper;

import com.jaegokeeper.hwan.domain.enums.StockHistoryType;
import org.apache.ibatis.annotations.Param;

public interface StockHistoryMapper {

//    @Param quantity는 변화량
    int insertHistory(@Param("stockId") Integer stockId,
                      @Param("hisType") StockHistoryType hisType,
                      @Param("delta") Integer quantity);
}
