package com.jaegokeeper.alba.controller;

import com.jaegokeeper.alba.dto.AlbaCreateResponse;
import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.service.AlbaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.jaegokeeper.auth.dto.LoginContext;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/albas")
public class StoreAlbaController {

    private final AlbaService albaService;

    @Operation(summary = "스토어별 알바 목록 조회")
    @GetMapping
    public ResponseEntity<List<AlbaListResponse>> getAlbas(
            @AuthenticationPrincipal LoginContext login) {
        return ResponseEntity.ok(albaService.getAllAlbaList(login));
    }

    @Operation(summary = "스토어별 알바 등록")
    @PostMapping
    public ResponseEntity<AlbaCreateResponse> createAlba(
            @Valid @ModelAttribute AlbaRegisterRequest req,
            @AuthenticationPrincipal LoginContext login) {
        int albaId = albaService.saveAlbaRegister(login, req);
        return ResponseEntity.status(201).body(new AlbaCreateResponse(albaId));
    }

    @Operation(summary = "스토어별 알바 상세 조회")
    @GetMapping("/{albaId}")
    public ResponseEntity<AlbaDetailResponse> getAlba(
            @PathVariable int albaId,
            @AuthenticationPrincipal LoginContext login) {
        return ResponseEntity.ok(albaService.getAlbaById(login, albaId));
    }

    @Operation(summary = "스토어별 알바 수정")
    @PutMapping("/{albaId}")
    public ResponseEntity<Void> updateAlba(
            @PathVariable int albaId,
            @Valid @RequestBody AlbaUpdateRequest req,
            @AuthenticationPrincipal LoginContext login) {
        req.setAlbaId(albaId);
        albaService.updateAlba(login, req);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "스토어별 알바 삭제")
    @DeleteMapping("/{albaId}")
    public ResponseEntity<Void> deleteAlba(
            @PathVariable int albaId,
            @AuthenticationPrincipal LoginContext login) {
        albaService.deleteAlba(login, albaId);
        return ResponseEntity.noContent().build();
    }
}
