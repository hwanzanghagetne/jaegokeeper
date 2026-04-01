package com.jaegokeeper.item.dto.response;

import com.jaegokeeper.stock.enums.LogType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ItemDetailResponse {

    private Integer itemId;
    private String itemName;
    private Integer stockAmount;
    private Boolean isPinned;
    private Integer imageId;
    private Integer bufferAmount;

    private LogType lastLogType;
    private LocalDateTime lastLogAt;
}
