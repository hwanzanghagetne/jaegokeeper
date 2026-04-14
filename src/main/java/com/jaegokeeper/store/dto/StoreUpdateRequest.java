package com.jaegokeeper.store.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class StoreUpdateRequest {

    private Integer storeId;

    @NotBlank(message = "매장명은 필수입니다.")
    @Size(max = 20, message = "매장명은 최대 20자까지 입력 가능합니다.")
    private String storeName;

    @Size(max = 255, message = "주소는 최대 255자까지 입력 가능합니다.")
    private String storeAdd1;

    @Size(max = 255, message = "상세 주소는 최대 255자까지 입력 가능합니다.")
    private String storeAdd2;

    @Size(max = 20, message = "매장 연락처는 최대 20자까지 입력 가능합니다.")
    private String storeTel;
}
