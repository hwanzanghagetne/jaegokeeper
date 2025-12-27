package com.jaegokeeper.psj.mapper;

import com.jaegokeeper.psj.dto.AlbaRegisterDto;
import com.jaegokeeper.psj.dto.ScheduleRegisterDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper {

    // schedule 저장
    int insertSchedule(ScheduleRegisterDto scheduleRegisterDto);
}
