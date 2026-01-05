package com.jaegokeeper.psj.controller;

import com.jaegokeeper.psj.dto.ScheduleListDto;
import com.jaegokeeper.psj.dto.ScheduleRegisterDto;
import com.jaegokeeper.psj.dto.ScheduleWorkInOutDto;
import com.jaegokeeper.psj.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
        scheduleService.saveScheduleRegister(scheduleRegisterDto);
        return ResponseEntity.ok(scheduleRegisterDto);
    }

//    // 프론트에서 전달받은 날짜를 요일로 변환 후 조회
//    @GetMapping
//    public ResponseEntity<List<ScheduleRegisterDto>> selectSchedulesByDate(@RequestParam String date) {
//        List<ScheduleRegisterDto> schedules = scheduleService.getScheduleByDate(date);
//        return ResponseEntity.ok(schedules);
//    }

    // 특정 날짜에 근무하는 알바생들의 출퇴근 기록 조회
    // list?date=2025-12-26 형태로 조회
    @GetMapping("/list")
    public ResponseEntity<List<ScheduleListDto>> selectScheduleListByDate(@RequestParam String date) {
        List<ScheduleListDto> scheduleList = scheduleService.getScheduleListByDate(date);
        return ResponseEntity.ok(scheduleList);
    }

    // 알바생 출/퇴근 시간 조회
    // worktime?albaId=2&date=2025-12-29 형태로 조회
    @GetMapping("/worktime")
    public ResponseEntity<List<ScheduleWorkInOutDto>> selectWorkTime(@RequestParam int albaId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ScheduleWorkInOutDto> workTime = scheduleService.selectWorkTime(albaId, date);
        return ResponseEntity.ok(workTime);
    }

    // 출근 기록
    // albaId & workStatus를 POST
    @PostMapping("/workin")
    public ResponseEntity<Void> recordWorkIn(@RequestBody ScheduleWorkInOutDto scheduleWorkInOutDto) {
        scheduleWorkInOutDto.setWorkIn(LocalDateTime.now());
        scheduleWorkInOutDto.setWorkDate(LocalDate.now());
        scheduleService.recordWorkIn(scheduleWorkInOutDto);  // ← Service 호출
        return ResponseEntity.ok().build();
    }

    // 퇴근 기록
    // albaId를 POST
    @PostMapping("/workout")
    public ResponseEntity<Void> recordWorkOut(@RequestBody ScheduleWorkInOutDto scheduleWorkInOutDto) {
        scheduleWorkInOutDto.setWorkOut(LocalDateTime.now());  // ← workOut으로 수정!
        scheduleWorkInOutDto.setWorkDate(LocalDate.now());
        scheduleService.recordWorkOut(scheduleWorkInOutDto);  // ← Service 호출
        return ResponseEntity.ok().build();
    }
}
