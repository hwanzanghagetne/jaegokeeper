package com.jaegokeeper.onboarding.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class SocialOwnerSignUpRequest {

    @NotBlank(message = "소셜 가입 토큰은 필수입니다")
    private String signupToken;

    @NotNull(message = "account 정보는 필수입니다")
    @Valid
    private AccountInfo account;

    @NotNull(message = "store 정보는 필수입니다")
    @Valid
    private StoreInfo store;

    @Getter
    @NoArgsConstructor
    public static class AccountInfo {
        @NotBlank @Size(max = 20)
        private String userName;

        @Size(max = 20)
        private String userPhone;
    }

    @Getter
    @NoArgsConstructor
    public static class StoreInfo {
        @NotBlank @Size(max = 20)
        private String storeName;

        @Size(max = 20)
        private String storeTel;

        @Size(max = 255)
        private String storeAdd1;

        @Size(max = 255)
        private String storeAdd2;
    }
}
