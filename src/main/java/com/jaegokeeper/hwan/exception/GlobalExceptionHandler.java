package com.jaegokeeper.hwan.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.jaegokeeper.hwan.exception.ErrorCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 검증 실패 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {

        List<FieldErrorResponse> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new FieldErrorResponse(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErrorResponse response = new ErrorResponse(BAD_REQUEST.getCode(),
                BAD_REQUEST.getMessage(),
                errors);

        return ResponseEntity
                .status(BAD_REQUEST.getStatus())
                .body(response);
    }

    // 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();

        if (errorCode.getStatus().is4xxClientError()) {
            log.warn("[BUSINESS EXCEPTION] code={} method={} uri={} param={}",
                    errorCode.getCode(),
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getParameterMap());
        } else {
            log.error("[BUSINESS EXCEPTION] code={} method={} uri={} param={}",
                    errorCode.getCode(),
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getParameterMap(),
                    e);
        }

        ErrorResponse response = new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    // 잘못된 인자
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("[BAD_REQUEST] (IllegalArgument) method={} uri={} param={} message={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getParameterMap(),
                e.getMessage());

        ErrorResponse response = new ErrorResponse(
                BAD_REQUEST.getCode(),
                BAD_REQUEST.getMessage(),
                null
        );

        return ResponseEntity
                .status(BAD_REQUEST.getStatus())
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {

        log.warn("[BAD_REQUEST] (NotReadable) method={} uri={} param={}",
                request.getMethod(), request.getRequestURI(), request.getParameterMap());

        ErrorResponse response = new ErrorResponse(
                BAD_REQUEST.getCode(),
                BAD_REQUEST.getMessage(),
                null
        );

        return ResponseEntity.status(BAD_REQUEST.getStatus()).body(response);
    }

    // 그 외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(Exception e, HttpServletRequest request) {

        log.error("[UNHANDLED_EXCEPTION] method={} uri={} param={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getParameterMap(),
                e);

        ErrorResponse response = new ErrorResponse(
                INTERNAL_ERROR.getCode(),
                INTERNAL_ERROR.getMessage(),
                null
        );

        return ResponseEntity
                .status(INTERNAL_ERROR.getStatus())
                .body(response);
    }
}
