package com.jaegokeeper.email.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class EmailAuthSendRequest {

    @NotBlank
    @Email
    private String email;


}
