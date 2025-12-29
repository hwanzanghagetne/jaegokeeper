package com.jaegokeeper.psj.service;

import com.jaegokeeper.psj.dto.ScheduleListDto;
import com.jaegokeeper.psj.dto.ScheduleRegisterDto;
import com.jaegokeeper.psj.dto.ScheduleWorkInOutDto;
import com.jaegokeeper.psj.mapper.AlbaMapper;
import com.jaegokeeper.psj.mapper.ScheduleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final AlbaMapper albaMapper;
    public ScheduleService(ScheduleMapper scheduleMapper, AlbaMapper albaMapper) {
        this.scheduleMapper = scheduleMapper;
        this.albaMapper = albaMapper;
    }

    // 스케줄에 등록 페이지
    @Transactional
    public int saveScheduleRegister(ScheduleRegisterDto scheduleRegisterDto) {

        // alba 존재 확인
        if(!albaMapper.existsAlbaById(scheduleRegisterDto.getAlbaId())) {
            throw new IllegalArgumentException("alba가 존재하지 않습니다.");
        }
        // schedule 저장 후 ID 반환
        int scheduleId = scheduleMapper.insertSchedule(scheduleRegisterDto);
        return scheduleId;
    }

    // 프론트에서 전달받은 날짜를 요일로 변환
    public List<ScheduleRegisterDto> getScheduleByDate(String date) {
        // 날짜를 요일로 변환
        DayOfWeek dayOfWeek = LocalDate.parse(date).getDayOfWeek();
        return scheduleMapper.selectSchedulesByDate(dayOfWeek.toString());
    }

    // 특정 날짜에 근무하는 알바생들의 출퇴근 기록 조회
    public List<ScheduleListDto> getScheduleListByDate(String date) {
        // 날짜를 요일로 변환
        DayOfWeek dayOfWeek = LocalDate.parse(date).getDayOfWeek();
        return scheduleMapper.selectSchedulesWithWorkByDate(date, dayOfWeek.toString());
    }

    // 출근 기록
    public void recordWorkIn(ScheduleWorkInOutDto scheduleWorkInOutDto) {
        scheduleMapper.insertWorkIn(scheduleWorkInOutDto);
    }

    // 퇴근 기록
    public void recordWorkOut(ScheduleWorkInOutDto scheduleWorkInOutDto) {
        scheduleMapper.updateWorkOut(scheduleWorkInOutDto);
    }
}
