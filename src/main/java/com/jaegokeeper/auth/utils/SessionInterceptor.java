package com.jaegokeeper.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.exception.ErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
        if (session == null) {
            return writeError(response, ErrorCode.LOGIN_REQUIRED);
        }

        Object loginObj = session.getAttribute(SESSION_KEY);
        if (!(loginObj instanceof LoginContext)) {
            return writeError(response, ErrorCode.LOGIN_REQUIRED);
        }

        // 3) storeId path variable이 있으면 로그인 세션의 storeId와 일치해야 함
        LoginContext login = (LoginContext) loginObj;
        @SuppressWarnings("unchecked")
        Map<String, String> uriTemplateVars =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (uriTemplateVars != null) {
            String pathStoreId = uriTemplateVars.get("storeId");
            if (pathStoreId != null) {
                int requestedStoreId;
                try {
                    requestedStoreId = Integer.parseInt(pathStoreId);
                } catch (NumberFormatException e) {
                    return writeError(response, ErrorCode.BAD_REQUEST);
                }

                if (requestedStoreId != login.getStoreId()) {
                    return writeError(response, ErrorCode.FORBIDDEN);
                }
            }
        }

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
