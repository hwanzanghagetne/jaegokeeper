package com.jaegokeeper.hwan.alba.mapper;

import com.jaegokeeper.hwan.alba.dto.AlbaOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlbaMapper2 {

    // request용 storeId로 알바생리스트
    List<AlbaOptionDTO> findAlbaOptionsForRequest(@Param("storeId") Integer storeId);

    int countByStoreIdAndAlbaId(@Param("storeId") Integer storeId,
                                @Param("albaId") Integer albaId);

    String findAlbaNameByAlbaId(@Param("albaId") Integer albaId);


}
