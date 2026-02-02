package com.jaegokeeper.auth.controller;

import com.jaegokeeper.auth.dto.AuthResponse;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.dto.SessionResponse;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/session")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping(value="/consume", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<SessionResponse> consume(
            @RequestParam("ticketKey") String key,
            @RequestParam("provider") String provider,
            HttpServletRequest request
    ) throws Exception {
        SessionResponse data = sessionService.handleSessionAuth(key, provider, request);
        return ApiResponse.ok(data);
    }

    @PostMapping(value="/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Void> logout(HttpSession session) {
        if (session != null) session.invalidate();
        return ApiResponse.ok(null);
    }

    @GetMapping(value="/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<SessionResponse> me(HttpSession session) {
        LoginContext login = (LoginContext) session.getAttribute("login");
        if (login == null) {
            return ApiResponse.fail("UNAUTHORIZED", "로그인이 필요합니다.");
        }

        return ApiResponse.ok(
                SessionResponse.builder()
                .userId(login.getUserId())
                .storeId(login.getStoreId())
                .userName(login.getUserName())
                .provider(login.getProvider())
                .build()
        );
    }

}
