package com.jaegokeeper.hwan.item.dto;

import com.jaegokeeper.hwan.item.domain.Item;
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

    @NotBlank(message = "itemName은 필수입니다.")
    private String itemName;

    @NotNull(message = "quantity는 필수입니다.")
    @Min(value = 0,message = "quantity는 0 이상입니다.")
    private Integer stockAmount;

    private Integer imageId;


}
