package com.jaegokeeper.psj.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDto {
    private int storeId;

    private int userId;

    private String storeName;

    private String storeAdd1;

    private String storeAdd2;

    private String storeTel;
}
