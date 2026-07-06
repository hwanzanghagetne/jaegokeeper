package com.jaegokeeper.email.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


@Getter
@Setter
public class EmailAuthVerifyRequest {

    @NotBlank
    @Email
    private String email;

    @NotNull
    @Pattern(regexp = "\\d{6}", message = "인증번호는 6자리 숫자여야 합니다.")
    private String code;
}
