package com.jaegokeeper.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.jaegokeeper.auth.dto.LoginRequest;
import com.jaegokeeper.auth.dto.SessionResponse;
import com.jaegokeeper.auth.enums.Provider;
import com.jaegokeeper.auth.service.LocalService;
import com.jaegokeeper.auth.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/local")
public class LocalController {

    private final LocalService localService;
    private final SessionService sessionService;

    @Operation(summary = "로컬 로그인", description = "이메일과 비밀번호로 로그인하고 세션을 생성합니다. JSON을 요구합니다. email, password를 받습니다.")
    @PostMapping(value="/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SessionResponse> login(
            @Validated @RequestBody LoginRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        int userId = localService.login(req);
        return ResponseEntity.ok(sessionService.createSession(userId, Provider.LOCAL.name(), request, response));
    }

}
