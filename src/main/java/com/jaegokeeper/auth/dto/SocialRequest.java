package com.jaegokeeper.auth.dto;

import com.jaegokeeper.auth.enums.Provider;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class SocialRequest {
    @NotNull(message = "provider는 필수입니다")
    private Provider provider;

    @NotBlank(message = "accessToken은 필수입니다")
    private String accessToken;
}
