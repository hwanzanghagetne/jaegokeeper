package com.jaegokeeper.email.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class EmailAuthSendRequest {

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;
}
