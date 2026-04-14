package com.jaegokeeper.alba.controller;

import com.jaegokeeper.alba.dto.AlbaCreateResponse;
import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.service.AlbaService;
import com.jaegokeeper.auth.annotation.LoginUser;
import com.jaegokeeper.auth.dto.LoginContext;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/albas")
public class StoreAlbaController {

    private final AlbaService albaService;

    @ApiOperation(value = "스토어별 알바 목록 조회")
    @GetMapping
    public ResponseEntity<List<AlbaListResponse>> getAlbas(
            @PathVariable int storeId,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(albaService.getAllAlbaList(login, storeId));
    }

    @ApiOperation(value = "스토어별 알바 등록")
    @PostMapping
    public ResponseEntity<AlbaCreateResponse> createAlba(
            @PathVariable int storeId,
            @Valid @ModelAttribute AlbaRegisterRequest req,
            @LoginUser LoginContext login) {
        req.setStoreId(storeId);
        int albaId = albaService.saveAlbaRegister(login, storeId, req);
        return ResponseEntity.status(201).body(new AlbaCreateResponse(albaId));
    }

    @ApiOperation(value = "스토어별 알바 상세 조회")
    @GetMapping("/{albaId}")
    public ResponseEntity<AlbaDetailResponse> getAlba(
            @PathVariable int storeId,
            @PathVariable int albaId,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(albaService.getAlbaById(login, storeId, albaId));
    }

    @ApiOperation(value = "스토어별 알바 수정")
    @PutMapping("/{albaId}")
    public ResponseEntity<Void> updateAlba(
            @PathVariable int storeId,
            @PathVariable int albaId,
            @RequestBody AlbaUpdateRequest req,
            @LoginUser LoginContext login) {
        req.setAlbaId(albaId);
        albaService.updateAlba(login, storeId, req);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "스토어별 알바 삭제")
    @DeleteMapping("/{albaId}")
    public ResponseEntity<Void> deleteAlba(
            @PathVariable int storeId,
            @PathVariable int albaId,
            @LoginUser LoginContext login) {
        albaService.deleteAlba(login, storeId, albaId);
        return ResponseEntity.noContent().build();
    }
}
