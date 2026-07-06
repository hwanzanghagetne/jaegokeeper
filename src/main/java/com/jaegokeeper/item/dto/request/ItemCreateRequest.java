package com.jaegokeeper.item.dto.request;

import com.jaegokeeper.image.dto.ImageInfoDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class ItemCreateRequest extends ImageInfoDTO {

    @NotBlank(message = "itemName은 필수입니다.")
    private String itemName;

    @NotNull(message = "quantity는 필수입니다.")
    @Min(value = 0, message = "quantity는 0 이상입니다.")
    private Integer stockAmount;
}
