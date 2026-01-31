package com.jaegokeeper.auth.dto;

import lombok.*;

import javax.validation.constraints.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;
}