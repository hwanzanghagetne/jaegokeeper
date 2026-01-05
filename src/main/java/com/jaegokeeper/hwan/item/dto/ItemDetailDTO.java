package com.jaegokeeper.hwan.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor

public class ItemDetailDTO {

    private Integer itemId;
    private Integer stockId;
    private String itemName;
    private Integer stockAmount;
    private Boolean isPinned;
    private String imageUrl;
    private Integer bufferAmount;

    private String lastLogType;
    private LocalDateTime lastLogAt;
}
