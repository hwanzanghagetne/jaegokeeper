package com.jaegokeeper.hwan.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
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

    // 그 외 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        //todo 로그 남기기
        log.error("Unhandled exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.",null));
    }
}
