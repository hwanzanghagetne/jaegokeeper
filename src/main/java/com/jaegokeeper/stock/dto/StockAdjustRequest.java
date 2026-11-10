package com.jaegokeeper.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import static com.jaegokeeper.stock.StockConstants.MAX_AMOUNT;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustRequest {

    @Min(0)
    @Max(MAX_AMOUNT)
    private Integer targetAmount;

    @Min(0)
    @Max(MAX_AMOUNT)
    private Integer bufferAmount;
}
