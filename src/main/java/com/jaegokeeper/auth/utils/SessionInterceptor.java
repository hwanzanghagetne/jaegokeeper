package com.jaegokeeper.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.exception.ErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

public class SessionInterceptor implements HandlerInterceptor {

    // 세션 키는 AuthService/AuthController에서 쓰던 것과 동일해야 함
    public static final String SESSION_KEY = "login";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1) 프리플라이트는 통과 (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2) 세션 존재/로그인 여부 체크
        // getSession(false): 없으면 새로 만들지 않음 (중요)
        var session = request.getSession(false);
        if (session != null && session.getAttribute(SESSION_KEY) != null) {
            return true;
        }

        // 3) 로그인 안 됨 -> JSON 401 응답
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(ErrorCode.LOGIN_REQUIRED.name(), ErrorCode.LOGIN_REQUIRED.getMessage(), null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        return false;
    }
}
