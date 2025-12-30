package com.jaegokeeper.psj.mapper;

import com.jaegokeeper.psj.dto.AlbaDetailDto;
import com.jaegokeeper.psj.dto.AlbaListDto;
import com.jaegokeeper.psj.dto.AlbaRegisterDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlbaMapper {
    void insertAlba(AlbaRegisterDto albaDto);

    boolean existsAlbaById(int albaId);
//
//    boolean existsWorkById(String workId);

    boolean existsByStoreId(int StoreId);

//    AlbaListDto selectsAlbaById(int albaId);
//
//    boolean existsByAlbaName(String albaName);

    int existsByAlbaPhone(String albaPhone);

    // 알바생 관리 페이지
    List<AlbaListDto> selectAllAlba(@Param("storeId") int storeId);

    // 알바생 등록 페이지
//    List<AlbaRegisterDto> selectRegisterAlba(@Param("albaId") int albaId);

    // 알바생 수정 페이지
    int updateAlba(AlbaDetailDto albaRegisterDto);

    // 알바생 삭제 페이지
    int deleteAlba(@Param("albaId") int albaId);

    // 알바생 상세 페이지
    AlbaDetailDto selectAlba(@Param("albaId") int albaId);

}
