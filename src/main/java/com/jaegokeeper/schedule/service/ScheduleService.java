package com.jaegokeeper.schedule.service;

import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.schedule.dto.ScheduleListDto;
import com.jaegokeeper.schedule.dto.ScheduleRegisterDto;
import com.jaegokeeper.schedule.dto.ScheduleWorkInOutDto;
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
    public int saveScheduleRegister(ScheduleRegisterDto scheduleRegisterDto) {
        if (!albaMapper.existsAlbaById(scheduleRegisterDto.getAlbaId())) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        return scheduleMapper.insertSchedule(scheduleRegisterDto);
    }

    @Transactional
    public void updateSchedule(ScheduleRegisterDto scheduleRegisterDto) {
        scheduleMapper.updateSchedule(scheduleRegisterDto);
    }

    public List<ScheduleRegisterDto> getScheduleByDate(String date) {
        DayOfWeek dayOfWeek = LocalDate.parse(date).getDayOfWeek();
        return scheduleMapper.selectSchedulesByDate(dayOfWeek.toString());
    }

    public List<ScheduleListDto> getScheduleListByDate(String date) {
        DayOfWeek dayOfWeek = LocalDate.parse(date).getDayOfWeek();
        return scheduleMapper.selectSchedulesWithWorkByDate(date, dayOfWeek.toString());
    }

    public List<ScheduleWorkInOutDto> selectWorkTime(int albaId, LocalDate date) {
        return scheduleMapper.selectWorkTime(albaId, date);
    }

    public void recordWorkIn(ScheduleWorkInOutDto scheduleWorkInOutDto) {
        scheduleMapper.insertWorkIn(scheduleWorkInOutDto);
    }

    public void recordWorkOut(ScheduleWorkInOutDto scheduleWorkInOutDto) {
        scheduleMapper.updateWorkOut(scheduleWorkInOutDto);
    }
}
