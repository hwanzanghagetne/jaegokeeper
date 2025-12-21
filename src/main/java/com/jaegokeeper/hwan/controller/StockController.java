package com.jaegokeeper.hwan.controller;

import com.jaegokeeper.hwan.dto.StockInOutRequestDTO;
import com.jaegokeeper.hwan.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;


    @PostMapping("/{stockId}/in")
    public ResponseEntity<Void> inStock(@PathVariable Integer stockId,
                                           @Valid @RequestBody StockInOutRequestDTO dto) {
        stockService.inStock(stockId,dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{stockId}/out")
    public ResponseEntity<Void> outStock(@PathVariable Integer stockId,
                                        @Valid @RequestBody StockInOutRequestDTO dto) {
        stockService.outStock(stockId,dto);
        return ResponseEntity.noContent().build();
    }
}
