package com.jaegokeeper.hwan.buffer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BufferMapper {
    int insertBuffer(
            @Param("itemId") Integer itemId,
            @Param("bufferAmount") Integer bufferAmount
    );

    int updateBufferAmount (
            @Param("itemId") Integer itemId,
            @Param("bufferAmount") Integer bufferAmount
    );

    Integer findBufferAmountByItemId(@Param("itemId") Integer itemId);

}
