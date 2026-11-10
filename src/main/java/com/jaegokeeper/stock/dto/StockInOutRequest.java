package com.jaegokeeper.stock.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import static com.jaegokeeper.stock.StockConstants.MAX_AMOUNT;

@Getter
@Setter
@NoArgsConstructor
public class StockInOutRequest {

    @NotNull(message = "amount는 필수입니다.")
    @Min(value = 1, message = "amount는 1 이상입니다.")
    @Max(value = MAX_AMOUNT, message = "amount는 1,000,000 이하입니다.")
    private Integer amount;
}
