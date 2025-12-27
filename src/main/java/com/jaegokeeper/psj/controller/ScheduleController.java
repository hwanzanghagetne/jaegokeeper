package com.jaegokeeper.psj.controller;

import com.jaegokeeper.psj.dto.ScheduleRegisterDto;
import com.jaegokeeper.psj.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    // 알바가 등록되면 해당 알바가 언제 근로하는지 스케줄 타임에 등록
    @PostMapping("/register")
    public ResponseEntity<ScheduleRegisterDto> saveScheduleRegister(@Valid @RequestBody ScheduleRegisterDto scheduleRegisterDto, HttpSession session) {
//        Integer albaId = (Integer) session.getAttribute("albaId");
//
//        // 세션에 없을 때 테스트용
//        if (albaId == null) {
//            albaId = 2;
//        }
//
//        scheduleRegisterDto.setAlbaId(albaId);
        scheduleService.saveScheduleRegister(scheduleRegisterDto);
        return ResponseEntity.ok(scheduleRegisterDto);
    }
}
