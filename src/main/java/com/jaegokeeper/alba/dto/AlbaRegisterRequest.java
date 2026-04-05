package com.jaegokeeper.alba.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class AlbaRegisterRequest {

    @NotNull(message = "스토어 아이디는 null입니다.")
    private Integer storeId;

    @NotBlank(message = "알바 이름은 필수입니다.")
    @Size(max = 50, message = "알바 이름은 최대 50자까지 입력 가능합니다.")
    private String albaName;

    @NotBlank(message = "알바 전화번호는 필수입니다.")
    @Size(max = 50, message = "알바 전화번호는 최대 50자까지 입력 가능합니다.")
    private String albaPhone;

    @Size(max = 50, message = "알바 근로상태는 최대 50자까지 입력 가능합니다.")
    private String albaStatus;

    private Integer imageId;

    private MultipartFile file;
}
