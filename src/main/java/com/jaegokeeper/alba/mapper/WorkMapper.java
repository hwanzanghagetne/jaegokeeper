package com.jaegokeeper.alba.mapper;

import com.jaegokeeper.alba.dto.AlbaListDto;
import com.jaegokeeper.alba.dto.AlbaRegisterDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkMapper {

    void insertWork(AlbaRegisterDto albaDto);

    int deleteWork(@Param("albaId") int albaId);

    List<AlbaListDto> selectAllWork(@Param("storeId") int storeId);
}
