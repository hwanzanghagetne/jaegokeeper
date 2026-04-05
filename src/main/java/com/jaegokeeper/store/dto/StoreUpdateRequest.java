package com.jaegokeeper.store.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreUpdateRequest {

    private Integer storeId;
    private String storeName;
    private String storeAdd1;
    private String storeAdd2;
    private String storeTel;
}
