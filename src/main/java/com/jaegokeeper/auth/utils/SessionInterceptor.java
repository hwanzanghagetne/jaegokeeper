package com.jaegokeeper.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.exception.ErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

public class SessionInterceptor implements HandlerInterceptor {

    // 세션에 로그인 정보를 저장하는 다른 곳(SessionService 등)에서도 이 상수를 참조해야 함
    public static final String SESSION_KEY = "login";

    private final ObjectMapper objectMapper;

    public SessionInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1) 프리플라이트는 통과 (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2) 세션 존재/로그인 여부 체크
        // getSession(false): 없으면 새로 만들지 않음 (중요)
        var session = request.getSession(false);
        if (session == null) {
            return writeError(response, ErrorCode.LOGIN_REQUIRED);
        }

        Object loginObj = session.getAttribute(SESSION_KEY);
        if (!(loginObj instanceof LoginContext)) {
            return writeError(response, ErrorCode.LOGIN_REQUIRED);
        }

        // storeId는 더 이상 URL로 받지 않는다. 로그인 세션(LoginContext.storeId)이
        // 유일한 출처이므로, 여기서는 "로그인된 세션이 있는가"만 확인하면 충분하다.
        return true;
    }

    private boolean writeError(HttpServletResponse response, ErrorCode errorCode) throws Exception {
        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(errorCode.name(), errorCode.getMessage(), null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        return false;
    }
}
