package com.jaegokeeper.auth.controller;

import com.jaegokeeper.auth.dto.*;
import com.jaegokeeper.auth.service.LocalAuthService;
import com.jaegokeeper.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/local")
public class LocalAuthController {

    private final LocalAuthService localAuthService;

    @PostMapping(value="/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<AuthResponse> register(@Validated @RequestBody RegisterRequest req) {
        return ApiResponse.ok(localAuthService.register(req));
    }

    @PostMapping(value="/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<AuthResponse> login(@Validated @RequestBody LoginRequest req, HttpSession session) {
        return ApiResponse.ok(localAuthService.login(req, session));
    }

    @PostMapping(value="/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Void> logout(HttpSession session) {
        localAuthService.logout(session);
        return ApiResponse.ok(null);
    }

    @GetMapping(value="/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<AuthResponse> me(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("LOGIN_USER_ID");
        if (userId == null) {
            return ApiResponse.fail("UNAUTHORIZED", "로그인이 필요합니다.");
        }
        String email = (String) session.getAttribute("LOGIN_EMAIL");
        String name = (String) session.getAttribute("LOGIN_NAME");
        return ApiResponse.ok(AuthResponse.builder().userId(userId).email(email).name(name).build());
    }
}
