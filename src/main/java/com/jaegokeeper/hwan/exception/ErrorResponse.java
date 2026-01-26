package com.jaegokeeper.hwan.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final String code;
    private final String message;
    private final List<FieldErrorResponse> errors;
}
