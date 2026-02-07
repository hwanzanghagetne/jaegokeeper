package com.jaegokeeper.hwan.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class ItemUpdateParamImg {

    private String itemName;
    private Boolean isPinned;

    private Integer imageId;
    private Boolean removeImage;


}
