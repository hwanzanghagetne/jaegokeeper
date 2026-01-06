package com.jaegokeeper.hwan.item.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
public class Item {

    private Integer itemId;
    private Integer storeId;
    private String itemName;
    private Integer imageId;

    private Boolean isActive;
    private Boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Item of (Integer storeId, String itemName, Integer imageId) {
        Item item = new Item();
        item.setStoreId(storeId);
        item.setItemName(itemName);
        item.setImageId(imageId);
        return item;
    }

}
