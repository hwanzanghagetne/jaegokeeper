package com.jaegokeeper.common.api;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final String code;
    private final int httpStatus;

    public ApiException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public static ApiException badRequest(String code, String message) { return new ApiException(code, message, 400); }
    public static ApiException unauthorized(String code, String message) { return new ApiException(code, message, 401); }
    public static ApiException conflict(String code, String message) { return new ApiException(code, message, 409); }
}
