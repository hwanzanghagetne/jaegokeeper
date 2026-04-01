package com.jaegokeeper.schedule.controller;

import com.jaegokeeper.schedule.dto.ScheduleListDto;
import com.jaegokeeper.schedule.dto.ScheduleRegisterDto;
import com.jaegokeeper.schedule.dto.ScheduleWorkInOutDto;
import com.jaegokeeper.schedule.service.ScheduleService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @ApiOperation(value = "알바 근무 스케줄 등록")
    @PostMapping("/register")
    public ResponseEntity<ScheduleRegisterDto> saveScheduleRegister(@Valid @RequestBody ScheduleRegisterDto scheduleRegisterDto) {
        scheduleService.saveScheduleRegister(scheduleRegisterDto);
        return ResponseEntity.ok(scheduleRegisterDto);
    }

    @ApiOperation(value = "알바 근무 스케줄 수정")
    @PutMapping("/register/{scheduleId}")
    public ResponseEntity<ScheduleRegisterDto> updateScheduleRegister(@PathVariable int scheduleId, @RequestBody ScheduleRegisterDto scheduleRegisterDto) {
        scheduleRegisterDto.setScheduleId(scheduleId);
        scheduleService.updateSchedule(scheduleRegisterDto);
        return ResponseEntity.ok(scheduleRegisterDto);
    }

    @ApiOperation(value = "특정 날짜에 근무하는 알바생들의 출퇴근 기록 조회")
    @GetMapping("/list")
    public ResponseEntity<List<ScheduleListDto>> selectScheduleListByDate(@RequestParam String date) {
        List<ScheduleListDto> scheduleList = scheduleService.getScheduleListByDate(date);
        return ResponseEntity.ok(scheduleList);
    }

    @ApiOperation(value = "알바생 출/퇴근 시간 조회")
    @GetMapping("/worktime")
    public ResponseEntity<List<ScheduleWorkInOutDto>> selectWorkTime(
            @RequestParam int albaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ScheduleWorkInOutDto> workTime = scheduleService.selectWorkTime(albaId, date);
        return ResponseEntity.ok(workTime);
    }

    @ApiOperation(value = "출근 기록")
    @PostMapping("/workin")
    public ResponseEntity<Void> recordWorkIn(@RequestBody ScheduleWorkInOutDto scheduleWorkInOutDto) {
        scheduleWorkInOutDto.setWorkIn(LocalDateTime.now());
        scheduleWorkInOutDto.setWorkDate(LocalDate.now());
        scheduleService.recordWorkIn(scheduleWorkInOutDto);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "퇴근 기록")
    @PostMapping("/workout")
    public ResponseEntity<Void> recordWorkOut(@RequestBody ScheduleWorkInOutDto scheduleWorkInOutDto) {
        scheduleWorkInOutDto.setWorkOut(LocalDateTime.now());
        scheduleWorkInOutDto.setWorkDate(LocalDate.now());
        scheduleService.recordWorkOut(scheduleWorkInOutDto);
        return ResponseEntity.ok().build();
    }
}
