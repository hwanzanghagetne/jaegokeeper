package com.jaegokeeper.onboarding.controller;

import com.jaegokeeper.onboarding.dto.OwnerSignUpRequest;
import com.jaegokeeper.onboarding.dto.OwnerSignUpResponse;
import com.jaegokeeper.onboarding.dto.SocialOwnerSignUpRequest;
import com.jaegokeeper.onboarding.service.OnboardingService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @ApiOperation(value = "점주 최초 가입", notes = "계정 생성과 점포 생성을 한 번에 처리합니다. 이메일 인증 완료 후 호출해야 합니다.")
    @PostMapping(value = "/owner-signup",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OwnerSignUpResponse> ownerSignUp(
            @Validated @RequestBody OwnerSignUpRequest req) {
        return ResponseEntity.ok(onboardingService.ownerSignUp(req));
    }

    @ApiOperation(value = "소셜 점주 추가정보 가입", notes = "소셜 인증 완료 후 발급된 signupToken으로 점주/점포 정보를 등록합니다.")
    @PostMapping(value = "/social-owner-signup",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OwnerSignUpResponse> socialOwnerSignUp(
            @Validated @RequestBody SocialOwnerSignUpRequest req) {
        return ResponseEntity.ok(onboardingService.socialOwnerSignUp(req));
    }
}
