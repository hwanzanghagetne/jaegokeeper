package com.jaegokeeper.hwan.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class StockResponse {

    private Integer stockId;
    private Integer stockAmount;
}
