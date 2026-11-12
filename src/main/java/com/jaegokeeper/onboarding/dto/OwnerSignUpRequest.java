package com.jaegokeeper.onboarding.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.nio.charset.StandardCharsets;

@Getter
@NoArgsConstructor
public class OwnerSignUpRequest {

    @NotNull(message = "account 정보는 필수입니다")
    @Valid
    private AccountInfo account;

    @NotNull(message = "store 정보는 필수입니다")
    @Valid
    private StoreInfo store;

    @Getter
    @NoArgsConstructor
    public static class AccountInfo {

        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 8, max = 72)
        private String password;

        @NotBlank @Size(max = 20)
        private String userName;

        @Size(max = 20)
        private String userPhone;

        @JsonIgnore
        @AssertTrue(message = "비밀번호는 72바이트 이하여야 합니다.")
        public boolean isPasswordWithinByteLimit() {
            return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
        }
    }
}
