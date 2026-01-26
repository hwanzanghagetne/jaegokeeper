package com.jaegokeeper.hwan.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ==== 400 BAD_REQUEST ====
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "요청 값이 올바르지 않습니다."),
    EMAIL_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_REGISTERED", "이미 가입된 이메일입니다."),
    EMAIL_CODE_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_CODE_INVALID", "인증번호가 올바르지 않거나 만료되었습니다."),

    // ==== 403 FORBIDDEN ====
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", "이메일 인증이 필요합니다."),
    ALBA_NOT_IN_STORE(HttpStatus.FORBIDDEN, "ALBA_NOT_IN_STORE", "해당 매장 직원이 아닙니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다."),

    // ==== 404 NOT_FOUND ====
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_NOT_FOUND", "게시글을 찾을 수 없습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "ITEM_NOT_FOUND", "아이템을 찾을 수 없습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE_NOT_FOUND", "매장을 찾을 수 없습니다."),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK_NOT_FOUND", "재고를 찾을 수 없습니다."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "REQUEST_NOT_FOUND", "요청을 찾을 수 없습니다."),

    // ===== 409 CONFLICT =====
    STATE_CONFLICT(HttpStatus.CONFLICT, "STATE_CONFLICT", "현재 상태에서는 처리할 수 없습니다."),

    // ===== 500 INTERNAL_SERVER_ERROR =====
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_UPLOAD_FAILED", "이미지 업로드에 실패했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}