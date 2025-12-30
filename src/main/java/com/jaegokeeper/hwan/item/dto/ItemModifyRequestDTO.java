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
public class ItemModifyRequestDTO {

    @NotNull(message = "storeId 필수입니다.")
    @Min(0)
    private Integer storeId;
    @NotNull(message = "itemId 필수입니다.")
    @Min(0)
    private Integer itemId;

    @NotBlank(message = "itemName은 필수입니다.")
    private String itemName;


    private Boolean favoriteYn;
    private Integer imageId;

    @NotNull(message = "safeQuantity 필수입니다.")
    @Min(value = 0,message = "safeQuantity는 0 이상입니다.")
    private Integer safeQuantity;

    @NotNull(message ="targetQuantity는 필수입니다." )
    @Min(value = 0,message = "targetQuantity는 0 이상입니다.")
    private Integer targetQuantity;
}

