package com.jaegokeeper.email.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class EmailAuthSendRequest {

    @NotBlank
    @Email
    private String email;


}
