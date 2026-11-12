package com.jaegokeeper.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.jaegokeeper.auth.enums.Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {

        List<FieldErrorResponse> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new FieldErrorResponse(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErrorResponse response = new ErrorResponse(BAD_REQUEST.name(),
                BAD_REQUEST.getMessage(),
                errors);

        return ResponseEntity
                .status(BAD_REQUEST.getStatus())
                .body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();

        if (errorCode.getStatus().is4xxClientError()) {
            log.warn("[BUSINESS EXCEPTION] code={} method={} uri={} param={}",
                    errorCode.name(),
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getParameterMap());
        } else {
            log.error("[BUSINESS EXCEPTION] code={} method={} uri={} param={}",
                    errorCode.name(),
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getParameterMap(),
                    e);
        }

        ErrorResponse response = new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                null
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("[BAD_REQUEST] (IllegalArgument) method={} uri={} param={} message={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getParameterMap(),
                e.getMessage());

        ErrorResponse response = new ErrorResponse(
                BAD_REQUEST.name(),
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

        if (e.getCause() instanceof InvalidFormatException ife
                && ife.getTargetType() == Provider.class) {
            ErrorResponse response = new ErrorResponse(
                    PROVIDER_INVALID.name(),
                    PROVIDER_INVALID.getMessage(),
                    null
            );
            return ResponseEntity.status(PROVIDER_INVALID.getStatus()).body(response);
        }

        ErrorResponse response = new ErrorResponse(
                BAD_REQUEST.name(),
                BAD_REQUEST.getMessage(),
                null
        );

        return ResponseEntity.status(BAD_REQUEST.getStatus()).body(response);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class})
    public ResponseEntity<ErrorResponse> handleBadParameter(Exception e, HttpServletRequest request) {
        return clientError(BAD_REQUEST, e, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(Exception e, HttpServletRequest request) {
        return clientError(METHOD_NOT_ALLOWED, e, request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(Exception e, HttpServletRequest request) {
        return clientError(UNSUPPORTED_MEDIA_TYPE, e, request);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception e, HttpServletRequest request) {
        return clientError(NOT_FOUND, e, request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleUploadTooLarge(Exception e, HttpServletRequest request) {
        return clientError(FILE_TOO_LARGE, e, request);
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrencyFailure(Exception e, HttpServletRequest request) {
        return clientError(STATE_CONFLICT, e, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(Exception e, HttpServletRequest request) {

        log.error("[UNHANDLED_EXCEPTION] method={} uri={} param={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getParameterMap(),
                e);

        ErrorResponse response = new ErrorResponse(
                INTERNAL_ERROR.name(),
                INTERNAL_ERROR.getMessage(),
                null
        );

        return ResponseEntity
                .status(INTERNAL_ERROR.getStatus())
                .body(response);
    }

    private ResponseEntity<ErrorResponse> clientError(ErrorCode errorCode, Exception e, HttpServletRequest request) {
        log.warn("[{}] method={} uri={} message={}",
                errorCode.name(), request.getMethod(), request.getRequestURI(), e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.name(), errorCode.getMessage(), null));
    }
}
