package com.jaegokeeper.stock.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.stock.dto.StockAmountUpdateRequest;
import com.jaegokeeper.stock.dto.StockDetailResponse;
import com.jaegokeeper.stock.dto.StockInOutRequest;
import com.jaegokeeper.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "Stock")
@RestController
@RequestMapping("/stores/items/{itemId}/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @Operation(summary = "재고 상세 조회")
    @GetMapping
    public ResponseEntity<StockDetailResponse> getStockDetail(
            @PathVariable Integer itemId,
            @AuthenticationPrincipal LoginContext login) {
        return ResponseEntity.ok(stockService.getStockDetail(login, itemId));
    }

    @Operation(summary = "재고 입고 처리")
    @PostMapping("/in")
    public ResponseEntity<Void> inStock(
            @PathVariable Integer itemId,
            @Valid @RequestBody StockInOutRequest dto,
            @AuthenticationPrincipal LoginContext login) {
        stockService.inStock(login, itemId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "재고 출고 처리")
    @PostMapping("/out")
    public ResponseEntity<Void> outStock(
            @PathVariable Integer itemId,
            @Valid @RequestBody StockInOutRequest dto,
            @AuthenticationPrincipal LoginContext login) {
        stockService.outStock(login, itemId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "재고 직접 조정")
    @PostMapping("/adjust")
    public ResponseEntity<Void> adjustStock(
            @PathVariable Integer itemId,
            @Valid @RequestBody StockAmountUpdateRequest dto,
            @AuthenticationPrincipal LoginContext login) {
        stockService.updateStockAmount(login, itemId, dto);
        return ResponseEntity.noContent().build();
    }
}
