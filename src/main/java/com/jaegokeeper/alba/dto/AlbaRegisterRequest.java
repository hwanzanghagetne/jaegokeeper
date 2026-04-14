package com.jaegokeeper.alba.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AlbaRegisterRequest {

    private Integer albaId;

    private Integer storeId;

    @NotBlank(message = "알바 이름은 필수입니다.")
    @Size(max = 50, message = "알바 이름은 최대 50자까지 입력 가능합니다.")
    private String albaName;

    @NotBlank(message = "알바 전화번호는 필수입니다.")
    @Size(max = 50, message = "알바 전화번호는 최대 50자까지 입력 가능합니다.")
    private String albaPhone;

    @Size(max = 50, message = "알바 근로상태는 최대 50자까지 입력 가능합니다.")
    private String albaStatus;

    @Email(message = "알바 이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "알바 이메일은 최대 255자까지 입력 가능합니다.")
    private String albaEmail;

    private Integer imageId;

    private List<String> scheduleTimes;

    private MultipartFile file;
}
