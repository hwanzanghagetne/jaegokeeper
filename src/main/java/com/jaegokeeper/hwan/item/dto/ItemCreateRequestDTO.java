package com.jaegokeeper.hwan.item.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class ItemCreateRequestDTO {

    @NotNull(message = "storeId는 필수입니다.")
    @Min(1)
    private Integer storeId;

    @NotBlank(message = "itemName은 필수입니다.")
    private String itemName;

    @NotNull(message = "quantity는 필수입니다.")
    @Min(value = 0,message = "quantity는 0 이상입니다.")
    private Integer quantity;

    private Integer imageId;

}
