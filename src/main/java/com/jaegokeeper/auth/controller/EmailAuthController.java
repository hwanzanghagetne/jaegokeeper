package com.jaegokeeper.auth.controller;

import com.jaegokeeper.auth.dto.EmailAuthSendRequest;
import com.jaegokeeper.auth.dto.EmailAuthVerifyRequest;
import com.jaegokeeper.auth.service.EmailAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
@Validated
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    /*코드 전송 */
    @PostMapping("/send")
    public ResponseEntity<Void> send(@Valid @RequestBody EmailAuthSendRequest dto) {
        emailAuthService.sendCode(dto.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@Valid @RequestBody EmailAuthVerifyRequest dto) {
        emailAuthService.verifyCode(dto.getEmail(), dto.getCode());

        return ResponseEntity.noContent().build();
    }
}
