package com.jaegokeeper.psj.mapper;

import com.jaegokeeper.psj.dto.AlbaListDto;
import com.jaegokeeper.psj.dto.AlbaRegisterDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkMapper {
    void insertWork(AlbaRegisterDto albaDto);

    // work 삭제
    int deleteWork(@Param("albaId") int albaId);


    List<AlbaListDto> selectAllWork(@Param("storeId") int storeId);

}