package com.jaegokeeper.alba.mapper;

import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlbaMapper {

    void insertAlba(AlbaRegisterRequest req);

    boolean existsAlbaById(int albaId);

    boolean existsByStoreId(int storeId);

    int existsByAlbaPhone(String albaPhone);

    List<AlbaListResponse> selectAllAlba(@Param("storeId") Integer storeId);

    int updateAlba(AlbaUpdateRequest req);

    int deleteAlba(@Param("albaId") int albaId);

    AlbaDetailResponse getAlbaById(@Param("albaId") int albaId);

    AlbaDetailResponse getAlbaByStore(@Param("storeId") int storeId);

    int countByStoreIdAndAlbaId(@Param("storeId") Integer storeId, @Param("albaId") Integer albaId);

    String findAlbaNameByAlbaId(@Param("albaId") Integer albaId);
}
