package com.jaegokeeper.hwan.stock.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class StockDetailResponse {

    private Integer stockId;
    private Integer itemId;
    private String itemName;
    private Integer stockAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
