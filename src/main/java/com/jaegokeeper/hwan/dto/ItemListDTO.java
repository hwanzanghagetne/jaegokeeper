package com.jaegokeeper.hwan.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ItemListDTO {
    //item
    private Integer itemId;
    private String itemName;

    //stock
    private Integer stockId;
    private Integer quantity;
    private Boolean favoriteYn;

    //이미지 일단 null
    private String imageUrl;


}
