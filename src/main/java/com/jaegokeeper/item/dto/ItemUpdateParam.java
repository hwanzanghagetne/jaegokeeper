package com.jaegokeeper.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemUpdateParam {

    private String itemName;
    private Boolean isPinned;
    private Integer imageId;
    private Boolean removeImage;
}
