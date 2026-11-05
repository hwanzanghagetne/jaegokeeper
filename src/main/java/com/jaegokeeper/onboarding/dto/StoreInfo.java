package com.jaegokeeper.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreInfo {

    @NotBlank @Size(max = 20)
    private String storeName;

    @Size(max = 20)
    private String storeTel;

    @Size(max = 255)
    private String storeAdd1;

    @Size(max = 255)
    private String storeAdd2;
}
