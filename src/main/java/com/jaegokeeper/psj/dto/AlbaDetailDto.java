package com.jaegokeeper.psj.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
