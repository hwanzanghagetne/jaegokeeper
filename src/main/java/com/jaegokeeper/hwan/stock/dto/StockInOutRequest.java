package com.jaegokeeper.hwan.stock.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class StockInOutRequest {

    @NotNull(message = "amount는 필수입니다.")
    @Min(value = 1,message = "amount는 1 이상입니다.")
    private Integer amount;

}
