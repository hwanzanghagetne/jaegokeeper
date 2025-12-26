package com.jaegokeeper.hwan.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SafeMapper {
    int insertSafe(@Param("stockId") Integer stockId,
                   @Param("safeQuantity") Integer safeQuantity);

    int updateSafe(@Param("stockId") Integer stockId,
                   @Param("safeQuantity") Integer safeQuantity);
}
