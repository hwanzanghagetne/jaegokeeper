package com.jaegokeeper.hwan.stock.controller;

import com.jaegokeeper.hwan.stock.dto.StockAmountUpdateRequest;
import com.jaegokeeper.hwan.stock.dto.StockDetailResponse;
import com.jaegokeeper.hwan.stock.dto.StockInOutRequest;
import com.jaegokeeper.hwan.stock.dto.StockResponse;
import com.jaegokeeper.hwan.stock.service.StockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Stock")
@RestController
@RequestMapping("/stores/{storeId}/items/{itemId}/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @ApiOperation(value = "재고 상세 조회", notes = "itemId 재고를 상세 조회합니다.")
    @GetMapping
    public ResponseEntity<StockDetailResponse> getStockDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId
    ) {
        return ResponseEntity.ok(stockService.getStockDetail(storeId, itemId));
    }

    @ApiOperation(value = "재고 입고 처리", notes = "itemId 재고에 입고 수량을 반영합니다.")
    @PostMapping("/in")
    public ResponseEntity<StockResponse> inStock(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId,
            @Valid @RequestBody StockInOutRequest dto)
    {
        stockService.inStock(storeId, itemId, dto);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "재고 출고 처리", notes = "itemId 재고에 출고 수량을 반영합니다.")
    @PostMapping("/out")
    public ResponseEntity<StockResponse> outStock(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId,
            @Valid @RequestBody StockInOutRequest dto)
    {
        stockService.outStock(storeId, itemId, dto);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "재고 직접 조정 처리", notes = "itemId 재고에 재고를 직접 조정합니다.")
    @PostMapping("/adjust")
    public ResponseEntity<StockResponse> adjustStock(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId,
            @Valid @RequestBody StockAmountUpdateRequest dto
    ) {
        stockService.updateStockAmount(storeId,itemId, dto);
        return ResponseEntity.noContent().build();
    }
}
