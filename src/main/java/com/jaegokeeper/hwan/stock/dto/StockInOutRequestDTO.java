package com.jaegokeeper.hwan.stock.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class StockInOutRequestDTO {

    @NotNull
    @Min(1)
    private Integer amount;

}
