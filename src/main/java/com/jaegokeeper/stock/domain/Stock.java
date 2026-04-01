package com.jaegokeeper.stock.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class Stock {

    @Setter
    private Integer stockId;

    private Integer itemId;
    private Integer stockAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Stock create(Integer itemId, Integer stockAmount) {
        Stock stock = new Stock();
        stock.itemId = itemId;
        stock.stockAmount = stockAmount;
        return stock;
    }
}
