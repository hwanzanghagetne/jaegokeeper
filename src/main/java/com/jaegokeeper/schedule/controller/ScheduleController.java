package com.jaegokeeper.schedule.controller;

import com.jaegokeeper.schedule.dto.ScheduleListResponse;
import com.jaegokeeper.schedule.dto.ScheduleRegisterRequest;
import com.jaegokeeper.schedule.dto.ScheduleUpdateRequest;
import com.jaegokeeper.schedule.dto.WorkInOutRequest;
import com.jaegokeeper.schedule.dto.WorkTimeResponse;
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
    public ResponseEntity<Void> saveScheduleRegister(@Valid @RequestBody ScheduleRegisterRequest req) {
        scheduleService.saveScheduleRegister(req);
        return ResponseEntity.status(201).build();
    }

    @ApiOperation(value = "알바 근무 스케줄 수정")
    @PutMapping("/register/{scheduleId}")
    public ResponseEntity<Void> updateScheduleRegister(@PathVariable int scheduleId, @RequestBody ScheduleUpdateRequest req) {
        req.setScheduleId(scheduleId);
        scheduleService.updateSchedule(req);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "특정 날짜에 근무하는 알바생들의 출퇴근 기록 조회")
    @GetMapping("/list")
    public ResponseEntity<List<ScheduleListResponse>> selectScheduleListByDate(@RequestParam String date) {
        return ResponseEntity.ok(scheduleService.getScheduleListByDate(date));
    }

    @ApiOperation(value = "알바생 출/퇴근 시간 조회")
    @GetMapping("/worktime")
    public ResponseEntity<List<WorkTimeResponse>> selectWorkTime(
            @RequestParam int albaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(scheduleService.selectWorkTime(albaId, date));
    }

    @ApiOperation(value = "출근 기록")
    @PostMapping("/workin")
    public ResponseEntity<Void> recordWorkIn(@RequestBody WorkInOutRequest req) {
        req.setWorkIn(LocalDateTime.now());
        req.setWorkDate(LocalDate.now());
        scheduleService.recordWorkIn(req);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "퇴근 기록")
    @PostMapping("/workout")
    public ResponseEntity<Void> recordWorkOut(@RequestBody WorkInOutRequest req) {
        req.setWorkOut(LocalDateTime.now());
        req.setWorkDate(LocalDate.now());
        scheduleService.recordWorkOut(req);
        return ResponseEntity.ok().build();
    }
}
