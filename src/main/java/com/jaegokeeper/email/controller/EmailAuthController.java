package com.jaegokeeper.email.controller;

import com.jaegokeeper.email.dto.EmailAuthSendRequest;
import com.jaegokeeper.email.dto.EmailAuthVerifyRequest;
import com.jaegokeeper.email.service.EmailAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Tag(name = "EmailAuth")
@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
@Validated
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    @Operation(summary = "이메일 인증코드 발송", description = "이메일로 인증코드를 발송합니다.")
    @PostMapping("/send")
    public ResponseEntity<Void> send(@Valid @RequestBody EmailAuthSendRequest dto) {
        emailAuthService.sendCode(dto.getEmail());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "이메일 인증코드 검증", description = "인증코드를 검증하고 인증 완료 처리합니다.")
    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@Valid @RequestBody EmailAuthVerifyRequest dto) {
        emailAuthService.verifyCode(dto.getEmail(), dto.getCode());

        return ResponseEntity.noContent().build();
    }
}
