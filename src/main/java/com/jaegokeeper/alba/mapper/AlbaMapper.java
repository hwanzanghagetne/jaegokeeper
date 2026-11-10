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

    boolean existsByStoreId(int storeId);

    int existsByAlbaPhone(@Param("storeId") Integer storeId,
                          @Param("albaPhone") String albaPhone,
                          @Param("excludeAlbaId") Integer excludeAlbaId);

    int existsByAlbaEmail(@Param("storeId") Integer storeId,
                          @Param("albaEmail") String albaEmail,
                          @Param("excludeAlbaId") Integer excludeAlbaId);

    List<AlbaListResponse> selectAllAlba(@Param("storeId") Integer storeId);

    int updateAlba(@Param("storeId") int storeId, @Param("req") AlbaUpdateRequest req);

    int deleteAlba(@Param("storeId") int storeId, @Param("albaId") int albaId);

    AlbaDetailResponse getAlbaById(@Param("storeId") int storeId, @Param("albaId") int albaId);

    int countByStoreIdAndAlbaId(@Param("storeId") Integer storeId, @Param("albaId") Integer albaId);

    String findAlbaNameByAlbaId(@Param("albaId") Integer albaId);
}
