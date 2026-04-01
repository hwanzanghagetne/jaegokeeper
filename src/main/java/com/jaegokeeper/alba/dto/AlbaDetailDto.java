package com.jaegokeeper.alba.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbaDetailDto {
    private Integer albaId;
    private Integer storeId;
    private String albaName;
    private String albaPhone;
    private String albaStatus;
}
