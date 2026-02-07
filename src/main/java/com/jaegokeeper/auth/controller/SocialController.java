package com.jaegokeeper.auth.controller;

import com.jaegokeeper.auth.dto.*;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.auth.service.SocialService;
import com.jaegokeeper.common.api.ApiResponse;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/social")
public class SocialController {

    private final SocialService socialService;
    private final SessionService sessionService;

    // 소셜 로그인
    @ApiOperation(value = "소셜 로그인", notes = "자초단 서비스에 로그인하고, 토큰 발급받아 세션에 등록합니다. JSON을 요구합니다. provider, accessToken 받습니다.")
    @PostMapping(value = "/complete",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<SessionResponse> complete(
            @Validated @RequestBody SocialRequest req,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            HttpServletRequest request
    ) throws Exception {
        String ticketKey = socialService.completeAndIssueTicket(
                req.getProvider().toUpperCase(), req.getAccessToken(), redirectUrl
        );

        SessionResponse data = sessionService.handleSessionAuth(ticketKey, req.getProvider(), request);
        return ApiResponse.ok(data);
    }

}