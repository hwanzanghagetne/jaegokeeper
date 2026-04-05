package com.jaegokeeper.alba.mapper;

import com.jaegokeeper.alba.dto.AlbaListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkMapper {

    int deleteWork(@Param("albaId") int albaId);

    List<AlbaListResponse> selectAllWork(@Param("storeId") int storeId);
}
