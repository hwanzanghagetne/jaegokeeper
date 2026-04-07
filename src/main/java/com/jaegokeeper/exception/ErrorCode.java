package com.jaegokeeper.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ==== 400 BAD_REQUEST ====
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    REGISTER_FAILED(HttpStatus.BAD_REQUEST, "회원가입 처리에 실패했습니다."),
    EMAIL_CODE_INVALID(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않거나 만료되었습니다."),
    INVALID_WRITER_INFO(HttpStatus.BAD_REQUEST, "writerType, writerId가 올바르지 않습니다."),
    IMAGE_UPDATE_CONFLICT(HttpStatus.BAD_REQUEST, "removeImage와 파일 업로드는 동시에 요청할 수 없습니다."),

    // ==== 401 UNAUTHORIZED ====
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_ACTIVE(HttpStatus.UNAUTHORIZED, "사용할 수 없는 계정 상태입니다."),

    // ==== 403 FORBIDDEN ====
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 필요합니다."),
    ALBA_NOT_IN_STORE(HttpStatus.FORBIDDEN, "해당 매장 직원이 아닙니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // ==== 404 NOT_FOUND ====
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "아이템을 찾을 수 없습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "재고를 찾을 수 없습니다."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "요청을 찾을 수 없습니다."),
    ALBA_NOT_FOUND(HttpStatus.NOT_FOUND, "알바생을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),

    // ==== 409 CONFLICT ====
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    STATE_CONFLICT(HttpStatus.CONFLICT, "현재 상태에서는 처리할 수 없습니다."),
    REQUEST_STATUS_NOT_WAIT(HttpStatus.CONFLICT, "대기 상태(WAIT)인 요청만 수정할 수 있습니다."),
    STOCK_QUANTITY_NOT_ENOUGH(HttpStatus.CONFLICT, "출고 수량이 현재 재고보다 많습니다."),


    // ===== 500 INTERNAL_SERVER_ERROR =====
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}