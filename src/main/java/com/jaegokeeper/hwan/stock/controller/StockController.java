package com.jaegokeeper.hwan.stock.controller;

import com.jaegokeeper.hwan.stock.dto.StockInOutRequestDTO;
import com.jaegokeeper.hwan.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/stores/{storeId}/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;


    @PostMapping("/{stockId}/in")
    public ResponseEntity<Void> inStock(
            @PathVariable Integer storeId,
            @PathVariable Integer stockId,
            @Valid @RequestBody StockInOutRequestDTO dto)
    {
        stockService.inStock(storeId,stockId, dto);
        return ResponseEntity.noContent().build();
    }

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
