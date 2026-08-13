package com.jaegokeeper.auth.controller;

import com.jaegokeeper.auth.dto.*;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.auth.service.SocialService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/social")
public class SocialController {

    private final SocialService socialService;
    private final SessionService sessionService;

    @Operation(summary = "소셜 로그인", description = "소셜 accessToken을 검증합니다. 기존 계정은 세션을 생성하고, 신규 계정은 추가정보 입력용 토큰을 반환합니다.")
    @PostMapping(value = "/complete",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> complete(
            @Validated @RequestBody SocialRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String provider = req.getProvider().name();
        SocialCompleteResponse result = socialService.complete(provider, req.getAccessToken());

        if (result.isSignupRequired()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
        }

        return ResponseEntity.ok(sessionService.createSession(result.getUserId(), provider, request, response));
    }

}
