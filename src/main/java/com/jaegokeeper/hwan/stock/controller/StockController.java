package com.jaegokeeper.hwan.stock.controller;

import com.jaegokeeper.hwan.stock.dto.StockInOutRequestDTO;
import com.jaegokeeper.hwan.stock.service.StockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Stock")
@RestController
@RequestMapping("/stores/{storeId}/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;


    @ApiOperation(value = "재고 입고 처리", notes = "stockId 재고에 입고 수량을 반영합니다.")
    @PostMapping("/{stockId}/in")
    public ResponseEntity<Void> inStock(
            @PathVariable Integer storeId,
            @PathVariable Integer stockId,
            @Valid @RequestBody StockInOutRequestDTO dto)
    {
        stockService.inStock(storeId,stockId, dto);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "재고 출고 처리", notes = "stockId 재고에 출고 수량을 반영합니다.")
    @PostMapping("/{stockId}/out")
    public ResponseEntity<Void> outStock(
            @PathVariable Integer storeId,
            @PathVariable Integer stockId,
            @Valid @RequestBody StockInOutRequestDTO dto)
    {
        stockService.outStock(storeId,stockId, dto);
        return ResponseEntity.noContent().build();
    }
}
