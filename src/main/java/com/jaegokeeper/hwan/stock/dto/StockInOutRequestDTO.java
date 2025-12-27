package com.jaegokeeper.hwan.stock.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class StockInOutRequestDTO {

    @NotNull
    @Min(1)
    private Integer amount;

}
