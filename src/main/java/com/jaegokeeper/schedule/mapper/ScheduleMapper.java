package com.jaegokeeper.schedule.mapper;

import com.jaegokeeper.schedule.dto.ScheduleListResponse;
import com.jaegokeeper.schedule.dto.ScheduleRegisterRequest;
import com.jaegokeeper.schedule.dto.ScheduleUpdateRequest;
import com.jaegokeeper.schedule.dto.WorkInOutRequest;
import com.jaegokeeper.schedule.dto.WorkTimeResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper {

    int insertSchedule(ScheduleRegisterRequest req);

    int updateSchedule(@Param("storeId") Integer storeId, @Param("req") ScheduleUpdateRequest req);

    List<ScheduleListResponse> selectSchedulesWithWorkByDate(
            @Param("storeId") Integer storeId,
            @Param("date") LocalDate date,
            @Param("scheduleTime") String scheduleTime
    );

    List<WorkTimeResponse> selectWorkTime(@Param("storeId") Integer storeId,
                                          @Param("albaId") Integer albaId,
                                          @Param("date") LocalDate date);

    int insertWorkIn(WorkInOutRequest req);

    int updateWorkOut(WorkInOutRequest req);
}
