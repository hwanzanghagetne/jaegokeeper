package com.jaegokeeper.hwan.mapper;

import org.apache.ibatis.annotations.Param;

public interface StoreMapper {

    int countById(@Param("storeId") Integer storeId);
}
