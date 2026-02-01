package com.jaegokeeper.hwan.item.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
public class Item {

    @Setter
    private Integer itemId;

    private Integer storeId;
    private String itemName;
    private Integer imageId;

    private Boolean isActive;
    private Boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Item create(Integer storeId, String itemName, Integer imageId) {
        Item item = new Item();
        item.storeId = storeId;
        item.itemName = itemName;
        item.imageId = imageId;
        return item;
    }

}
