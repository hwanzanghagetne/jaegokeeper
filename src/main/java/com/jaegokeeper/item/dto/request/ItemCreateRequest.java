package com.jaegokeeper.item.dto.request;

import com.jaegokeeper.image.dto.ImageInfoDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static com.jaegokeeper.stock.StockConstants.MAX_AMOUNT;

@Getter
@Setter
@NoArgsConstructor
public class ItemCreateRequest extends ImageInfoDTO {

    @NotBlank(message = "itemName은 필수입니다.")
    @Size(max = 50, message = "itemName은 50자 이하입니다.")
    private String itemName;

    @NotNull(message = "stockAmount는 필수입니다.")
    @Min(value = 0, message = "stockAmount는 0 이상입니다.")
    @Max(value = MAX_AMOUNT, message = "stockAmount는 1,000,000 이하입니다.")
    private Integer stockAmount;
}
