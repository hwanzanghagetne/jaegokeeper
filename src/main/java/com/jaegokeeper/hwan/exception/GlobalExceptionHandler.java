package com.jaegokeeper.hwan.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 검증 실패 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {

        if (e.getBindingResult().getFieldErrors().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("VALIDATION_ERROR", "요청 값이 올바르지 않습니다.", null));
        }

        FieldError fe = e.getBindingResult().getFieldErrors().get(0);
        String field = fe.getField();
        String msg = fe.getDefaultMessage();

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", msg, field));
    }

    // 잘못된 요청 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleException(IllegalArgumentException e) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("INVALID_ARGUMENT", e.getMessage(), null));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(NotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", e.getMessage(), null));
    }
    // 그 외 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {

        //todo 로그 남기기


        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.", null));
    }
}
