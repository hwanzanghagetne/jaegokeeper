package com.jaegokeeper.onboarding.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerSignUpResponse {

    private Integer userId;
    private Integer storeId;
    private String userName;
    private String storeName;
}
