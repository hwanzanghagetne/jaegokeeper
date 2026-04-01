package com.jaegokeeper.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustRequest {

    @Min(0)
    private Integer targetAmount;

    @Min(0)
    private Integer bufferAmount;
}
