package com.jaegokeeper.alba.mapper;

import com.jaegokeeper.alba.dto.AlbaDetailDto;
import com.jaegokeeper.alba.dto.AlbaListDto;
import com.jaegokeeper.alba.dto.AlbaRegisterDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlbaMapper {

    void insertAlba(AlbaRegisterDto albaDto);

    boolean existsAlbaById(int albaId);

    boolean existsByStoreId(int storeId);

    int existsByAlbaPhone(String albaPhone);

    List<AlbaListDto> selectAllAlba(@Param("storeId") Integer storeId);

    int updateAlba(AlbaDetailDto albaDetailDto);

    int deleteAlba(@Param("albaId") int albaId);

    AlbaDetailDto getAlbaById(@Param("albaId") int albaId);

    AlbaDetailDto getAlbaByStore(@Param("storeId") int storeId);

    // board에서 사용 (AlbaMapper2 통합)
    int countByStoreIdAndAlbaId(@Param("storeId") Integer storeId, @Param("albaId") Integer albaId);

    String findAlbaNameByAlbaId(@Param("albaId") Integer albaId);
}
