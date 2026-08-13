package com.jaegokeeper.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 보호 API에 비로그인 상태로 접근했을 때의 401 응답을, 기존 SessionInterceptor.writeError()와
 * 동일한 JSON 계약(ErrorCode.LOGIN_REQUIRED)으로 유지하기 위한 EntryPoint.
 * 이게 없으면 Spring Security 기본값(403 Http403ForbiddenEntryPoint)으로 계약이 깨진다.
 */
@Component
@RequiredArgsConstructor
public class LoginRequiredEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        ErrorCode errorCode = ErrorCode.LOGIN_REQUIRED;
        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(errorCode.name(), errorCode.getMessage(), null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
