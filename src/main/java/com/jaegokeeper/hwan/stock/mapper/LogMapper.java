package com.jaegokeeper.hwan.stock.mapper;

import com.jaegokeeper.hwan.stock.enums.LogType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LogMapper {

//    @Param quantity는 변화량
    int insertLog(@Param("itemId") Integer itemId,
                  @Param("logType") LogType logType,
                  @Param("amount") Integer amount);
}
