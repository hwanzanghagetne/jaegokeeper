package com.jaegokeeper.schedule.service;

import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.schedule.dto.ScheduleDetailResponse;
import com.jaegokeeper.schedule.dto.ScheduleListResponse;
import com.jaegokeeper.schedule.dto.ScheduleRegisterRequest;
import com.jaegokeeper.schedule.dto.ScheduleUpdateRequest;
import com.jaegokeeper.schedule.dto.WorkInOutRequest;
import com.jaegokeeper.schedule.dto.WorkTimeResponse;
import com.jaegokeeper.schedule.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.exception.ErrorCode.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final AlbaMapper albaMapper;

    @Transactional
    public int saveScheduleRegister(ScheduleRegisterRequest req) {
        if (!albaMapper.existsAlbaById(req.getAlbaId())) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        return scheduleMapper.insertSchedule(req);
    }

    @Transactional
    public void updateSchedule(ScheduleUpdateRequest req) {
        scheduleMapper.updateSchedule(req);
    }

    public List<ScheduleDetailResponse> getScheduleByDate(String date) {
        DayOfWeek dayOfWeek = LocalDate.parse(date).getDayOfWeek();
        return scheduleMapper.selectSchedulesByDate(dayOfWeek.toString());
    }

    public List<ScheduleListResponse> getScheduleListByDate(String date) {
        DayOfWeek dayOfWeek = LocalDate.parse(date).getDayOfWeek();
        return scheduleMapper.selectSchedulesWithWorkByDate(date, dayOfWeek.toString());
    }

    public List<WorkTimeResponse> selectWorkTime(int albaId, LocalDate date) {
        return scheduleMapper.selectWorkTime(albaId, date);
    }

    public void recordWorkIn(WorkInOutRequest req) {
        scheduleMapper.insertWorkIn(req);
    }

    public void recordWorkOut(WorkInOutRequest req) {
        scheduleMapper.updateWorkOut(req);
    }
}
