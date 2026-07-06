package com.jaegokeeper.stock.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class StockAmountUpdateRequest {

    @NotNull(message = "stockAmount는 필수입니다.")
    @Min(value = 0, message = "stockAmount는 0 이상입니다.")
    private Integer stockAmount;
}
