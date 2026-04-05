package com.jaegokeeper.alba.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlbaDetailResponse {

    private Integer albaId;
    private Integer storeId;
    private String albaName;
    private String albaPhone;
    private String albaStatus;
}
