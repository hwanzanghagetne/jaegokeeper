package com.jaegokeeper.hwan.item.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StoreMapper {

    int countById(@Param("storeId") Integer storeId);
}
