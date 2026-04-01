package com.jaegokeeper.stock.mapper;

import com.jaegokeeper.stock.enums.LogType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LogMapper {

    int insertLog(@Param("itemId") Integer itemId,
                  @Param("logType") LogType logType,
                  @Param("amount") Integer amount);
}
