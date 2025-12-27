package com.jaegokeeper.hwan.item.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ItemCreateRequestDTO {

    @NotNull(message = "storeId는 필수입니다.")
    private Integer storeId;

    @NotBlank(message = "itemName은 필수입니다.")
    private String itemName;

    @NotNull(message = "quantity는 필수입니다.")
    private Integer quantity;

    private Integer imageId;

}
