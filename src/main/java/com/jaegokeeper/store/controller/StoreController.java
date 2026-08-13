package com.jaegokeeper.store.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.store.dto.StoreDetailResponse;
import com.jaegokeeper.store.dto.StoreUpdateRequest;
import com.jaegokeeper.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "점포 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<StoreDetailResponse> getStore(
            @AuthenticationPrincipal LoginContext login) {
        return ResponseEntity.ok(storeService.getStoreDetail(login));
    }

    @Operation(summary = "점포 정보 수정")
    @PutMapping("/me")
    public ResponseEntity<Void> updateStore(
            @Valid @RequestBody StoreUpdateRequest req,
            @AuthenticationPrincipal LoginContext login) {
        storeService.updateStore(login, req);
        return ResponseEntity.noContent().build();
    }
}
