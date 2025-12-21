package com.jaegokeeper.hwan.dto;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ItemModifyRequestDTO {



    @NotBlank(message = "itemName은 필수입니다.")
    private String itemName;

    @NotNull
    private Integer stockId;

    @NotNull
    private Boolean favoriteYn;

    private Integer imageId;

    @NotNull
    @Min(0)
    private Integer safeQuantity;

    @NotNull
    @Min(0)
    private Integer targetQuantity;
}

