package com.jaegokeeper.hwan.item.dto.request;


import com.jaegokeeper.ddan.img.dto.ImgInfoDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class ItemUpdateRequest extends ImgInfoDTO {


    private String itemName;
    private Boolean isPinned;

    private Boolean removeImage;

    @Min(value = 0, message = "bufferAmount는 0 이상입니다.")
    private Integer bufferAmount;
    @Min(value = 0, message = "targetAmount 0 이상입니다.")
    private Integer targetAmount;
}

