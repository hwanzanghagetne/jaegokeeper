package com.jaegokeeper.psj.service;

import com.jaegokeeper.psj.dto.ScheduleRegisterDto;
import com.jaegokeeper.psj.mapper.AlbaMapper;
import com.jaegokeeper.psj.mapper.ScheduleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


//        scheduleMapper.insertSchedule(scheduleRegisterDto);



    }
}
