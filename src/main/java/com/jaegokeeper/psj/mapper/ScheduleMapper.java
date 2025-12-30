package com.jaegokeeper.psj.mapper;

import com.jaegokeeper.psj.dto.AlbaRegisterDto;
import com.jaegokeeper.psj.dto.ScheduleListDto;
import com.jaegokeeper.psj.dto.ScheduleRegisterDto;
import com.jaegokeeper.psj.dto.ScheduleWorkInOutDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper {

    // schedule 저장
    int insertSchedule(ScheduleRegisterDto scheduleRegisterDto);

    // 프론트에서 전달받은 날짜를 요일로 변환
    List<ScheduleRegisterDto> selectSchedulesByDate(@Param("scheduleTime") String scheduleTime);

    // 특정 날짜에 근무하는 알바생들의 출퇴근 기록 조회
    List<ScheduleListDto> selectSchedulesWithWorkByDate(
            @Param("date") String date,
            @Param("scheduleTime") String scheduleTime
    );

    // 알바생 출/퇴근 시간 조회
    // List로 변경
    List<ScheduleWorkInOutDto> selectWorkTime(@Param("albaId") Integer albaId, @Param("date") LocalDate date);

    // 출근 기록
    void insertWorkIn(ScheduleWorkInOutDto dto);

    // 퇴근 기록
    void updateWorkOut(ScheduleWorkInOutDto dto);
}
