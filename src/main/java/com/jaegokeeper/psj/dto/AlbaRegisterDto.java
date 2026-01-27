package com.jaegokeeper.psj.dto;

import com.jaegokeeper.ddan.img.dto.ImgInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbaRegisterDto extends ImgInfoDTO {

    @NotNull(message = "스토어 아이디는 null입니다.")
    private Integer storeId;

    private Integer workId;

    private Integer albaId;

    @NotBlank(message = "알바 이름은 필수입니다.")
    @Size(max = 50, message = "아이템 이름은 최대 50자까지 입력 가능합니다.")
    private String albaName;

    @NotBlank(message = "알바 전화번호는 필수입니다.")
    @Size(max = 50, message = "아이템 이름은 최대 50자까지 입력 가능합니다.")
    private String albaPhone;

    @Size(max = 50, message = "알바 근로상태는 최대 50자까지 입력 가능합니다.")
    private String albaStatus;

//    @NotBlank(message = "근무일은 필수입니다.")
//    @Size(max = 50, message = "근무일은 최대 50자까지 입력 가능합니다.")
//    private String workDate;
//
//    @NotBlank(message = "알바 근태는 필수입니다.")
//    @Size(max = 50, message = "알바근태는 최대 50자까지 입력 가능합니다.")
//    private String workStatus;

}
