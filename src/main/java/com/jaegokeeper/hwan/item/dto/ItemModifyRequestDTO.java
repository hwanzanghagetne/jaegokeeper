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



    @NotBlank(message = "itemName은 필수입니다.")
    private String itemName;

    @NotNull
    @Min(1)
    private Integer stockId;

    private Boolean favoriteYn;

    private Integer imageId;

    @NotNull
    @Min(0)
    private Integer safeQuantity;

    @NotNull
    @Min(0)
    private Integer targetQuantity;
}

