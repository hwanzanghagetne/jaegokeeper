package com.jaegokeeper.auth.controller;

import com.jaegokeeper.auth.dto.*;
import com.jaegokeeper.auth.service.LocalService;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/local")
public class LocalController {

    private final LocalService localService;
    private final SessionService sessionService;

    @PostMapping(value="/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<AuthResponse> register(@Validated @RequestBody RegisterRequest req) throws Exception {
        return ApiResponse.ok(localService.register(req));
    }

    @PostMapping(value="/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<SessionResponse> login(
            @Validated @RequestBody LoginRequest req,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            HttpServletRequest request
    ) throws Exception {
        String ticketKey = localService.loginAndIssueTicket(req, redirectUrl);

        SessionResponse data = sessionService.handleSessionAuth(ticketKey, "LOCAL", request);
        return ApiResponse.ok(data);
    }

}
