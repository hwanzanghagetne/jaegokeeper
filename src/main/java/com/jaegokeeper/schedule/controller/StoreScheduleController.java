package com.jaegokeeper.schedule.controller;

import com.jaegokeeper.auth.annotation.LoginUser;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.schedule.dto.ScheduleListResponse;
import com.jaegokeeper.schedule.dto.ScheduleRegisterRequest;
import com.jaegokeeper.schedule.dto.ScheduleUpdateRequest;
import com.jaegokeeper.schedule.dto.WorkInOutRequest;
import com.jaegokeeper.schedule.dto.WorkTimeResponse;
import com.jaegokeeper.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/stores/schedules")
@RequiredArgsConstructor
public class StoreScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "스토어별 알바 근무 스케줄 등록")
    @PostMapping
    public ResponseEntity<Void> createSchedule(
            @Valid @RequestBody ScheduleRegisterRequest req,
            @LoginUser LoginContext login) {
        scheduleService.saveScheduleRegister(login, req);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "스토어별 알바 근무 스케줄 수정")
    @PutMapping("/{scheduleId}")
    public ResponseEntity<Void> updateSchedule(
            @PathVariable int scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest req,
            @LoginUser LoginContext login) {
        req.setScheduleId(scheduleId);
        scheduleService.updateSchedule(login, req);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "스토어별 특정 날짜 스케줄 조회")
    @GetMapping
    public ResponseEntity<List<ScheduleListResponse>> getSchedulesByDate(
            @RequestParam String date,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(scheduleService.getScheduleListByDate(login, date));
    }

    @Operation(summary = "스토어별 알바 출/퇴근 시간 조회")
    @GetMapping("/worktime")
    public ResponseEntity<List<WorkTimeResponse>> getWorkTime(
            @RequestParam int albaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(scheduleService.selectWorkTime(login, albaId, date));
    }

    @Operation(summary = "스토어별 출근 기록")
    @PostMapping("/workin")
    public ResponseEntity<Void> recordWorkIn(
            @Valid @RequestBody WorkInOutRequest req,
            @LoginUser LoginContext login) {
        scheduleService.recordWorkIn(login, req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스토어별 퇴근 기록")
    @PostMapping("/workout")
    public ResponseEntity<Void> recordWorkOut(
            @Valid @RequestBody WorkInOutRequest req,
            @LoginUser LoginContext login) {
        scheduleService.recordWorkOut(login, req);
        return ResponseEntity.ok().build();
    }
}
