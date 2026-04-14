package com.jaegokeeper.auth.controller;

import com.jaegokeeper.auth.dto.LoginRequest;
import com.jaegokeeper.auth.dto.SessionResponse;
import com.jaegokeeper.auth.service.LocalService;
import com.jaegokeeper.auth.service.SessionService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/local")
public class LocalController {

    private final LocalService localService;
    private final SessionService sessionService;

    // 로컬 로그인
    @ApiOperation(value = "로컬 로그인", notes = "자초단 서비스에 로그인하고, 토큰 발급받아 세션에 등록합니다. JSON을 요구합니다. email, password 받습니다.")
    @PostMapping(value="/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SessionResponse> login(
            @Validated @RequestBody LoginRequest req,
            HttpServletRequest request
    ) {
        int userId = localService.login(req);
        return ResponseEntity.ok(sessionService.createSession(userId, "LOCAL", request));
    }

}
