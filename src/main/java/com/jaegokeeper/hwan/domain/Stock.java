package com.jaegokeeper.hwan.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Stock {

    private Integer stockId;

    private Integer itemId;

    private Integer storeId;

    private Integer quantity;

    private Boolean favoriteYn;
}
