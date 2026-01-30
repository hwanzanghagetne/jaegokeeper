package com.jaegokeeper.hwan.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockDetailResponse {

    private Integer stockId;
    private Integer itemId;
    private String itemName;
    private Integer stockAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
