package com.jaegokeeper.hwan.stock.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Stock {

    private Integer stockId;
    private Integer itemId;
    private Integer stockAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Stock of(Integer itemId, Integer stockAmount) {
        Stock stock = new Stock();
        stock.setItemId(itemId);
        stock.setStockAmount(stockAmount);
        return stock;
    }
}
