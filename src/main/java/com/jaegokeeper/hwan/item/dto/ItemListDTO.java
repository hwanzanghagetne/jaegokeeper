package com.jaegokeeper.hwan.item.dto;

import com.jaegokeeper.hwan.request.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemListDTO {
    //item
    private Integer itemId;
    private String itemName;

    //stock
    private Integer stockId;
    private Integer stockAmount;
    private Boolean isPinned;

    //이미지 일단 null
    private String imageUrl;

    private RequestType latestRequestType;


}
