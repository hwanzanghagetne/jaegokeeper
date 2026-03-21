package com.jaegokeeper.hwan.alba.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AlbaMapper2 {

    int countByStoreIdAndAlbaId(@Param("storeId") Integer storeId,
                                @Param("albaId") Integer albaId);

    String findAlbaNameByAlbaId(@Param("albaId") Integer albaId);


}
