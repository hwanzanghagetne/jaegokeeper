package com.jaegokeeper.schedule.mapper;

import com.jaegokeeper.schedule.dto.ScheduleListDto;
import com.jaegokeeper.schedule.dto.ScheduleRegisterDto;
import com.jaegokeeper.schedule.dto.ScheduleWorkInOutDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper {

    int insertSchedule(ScheduleRegisterDto scheduleRegisterDto);

    void updateSchedule(ScheduleRegisterDto scheduleRegisterDto);

    List<ScheduleRegisterDto> selectSchedulesByDate(@Param("scheduleTime") String scheduleTime);

    List<ScheduleListDto> selectSchedulesWithWorkByDate(
            @Param("date") String date,
            @Param("scheduleTime") String scheduleTime
    );

    List<ScheduleWorkInOutDto> selectWorkTime(@Param("albaId") Integer albaId,
                                              @Param("date") LocalDate date);

    void insertWorkIn(ScheduleWorkInOutDto dto);

    void updateWorkOut(ScheduleWorkInOutDto dto);
}
