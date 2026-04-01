package com.jaegokeeper.item.dto.request;

import com.jaegokeeper.image.dto.ImageInfoDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;

@Getter
@Setter
@NoArgsConstructor
public class ItemUpdateRequest extends ImageInfoDTO {

    private String itemName;
    private Boolean isPinned;
    private Boolean removeImage;

    @Min(value = 0, message = "bufferAmount는 0 이상입니다.")
    private Integer bufferAmount;

    @Min(value = 0, message = "targetAmount 0 이상입니다.")
    private Integer targetAmount;
}
