package com.jaegokeeper.store.controller;

import com.jaegokeeper.auth.annotation.LoginUser;
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
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDetailResponse> getStore(
            @PathVariable int storeId,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(storeService.getStoreDetail(login, storeId));
    }

    @Operation(summary = "점포 정보 수정")
    @PutMapping("/{storeId}")
    public ResponseEntity<Void> updateStore(
            @PathVariable int storeId,
            @Valid @RequestBody StoreUpdateRequest req,
            @LoginUser LoginContext login) {
        storeService.updateStore(login, storeId, req);
        return ResponseEntity.noContent().build();
    }
}
