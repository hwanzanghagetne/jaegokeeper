package com.jaegokeeper.hwan.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class Item {

    private Integer itemId;

    private Integer storeId;

    private String itemName;

    private Integer imageId;

}
