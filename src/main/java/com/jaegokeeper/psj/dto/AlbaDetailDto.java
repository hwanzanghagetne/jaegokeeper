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


    @NotBlank(message = "알바 이름은 필수입니다.")
    @Size(max = 50, message = "아이템 이름은 최대 50자까지 입력 가능합니다.")
    private String albaName;

    @NotBlank(message = "알바 전화번호는 필수입니다.")
    @Size(max = 50, message = "아이템 이름은 최대 50자까지 입력 가능합니다.")
    private String albaPhone;

    @NotBlank(message = "알바 근로상태는 필수입니다.")
    @Size(max = 50, message = "알바 근로상태는 최대 50자까지 입력 가능합니다.")
    private String albaStatus;

}
