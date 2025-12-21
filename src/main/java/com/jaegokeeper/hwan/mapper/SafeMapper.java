package com.jaegokeeper.hwan.mapper;

import org.apache.ibatis.annotations.Param;

public interface SafeMapper {
    int insertSafe(@Param("stockId") Integer stockId,
                   @Param("safeQuantity") Integer safeQuantity);

    int updateSafe(@Param("stockId") Integer stockId,
                   @Param("safeQuantity") Integer safeQuantity);
}
