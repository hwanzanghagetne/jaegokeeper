package com.jaegokeeper.auth.controller;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.dto.SessionResponse;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.exception.BusinessException;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.jaegokeeper.exception.ErrorCode.LOGIN_REQUIRED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/session")
public class SessionController {

    private final SessionService sessionService;

    // 토큰으로 인가하기
    @ApiOperation(value = "토큰으로 인가하기", notes = "로그인 시도할 때 자동으로 소모합니다. 호출하지 않아도 됩니다.")
    @PostMapping(value="/consume", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SessionResponse> consume(
            @RequestParam("ticketKey") String key,
            @RequestParam("provider") String provider,
            HttpServletRequest request
    ) {
        SessionResponse data = sessionService.handleSessionAuth(key, provider, request);
        return ResponseEntity.ok(data);
    }

    // 로그아웃
    @ApiOperation(value = "로그아웃", notes = "로그인된 세션을 클린업합니다. POST요청으로 바디X")
    @PostMapping(value="/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        if (session != null) session.invalidate();
        return ResponseEntity.noContent().build();
    }

    // 로그미
    @ApiOperation(value = "유저정보", notes = "현재 등록된 세션 기준으로 유저 정보를 조회합니다. GET요청으로 바디X")
    @GetMapping(value="/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SessionResponse> me(HttpSession session) {
        LoginContext login = (session != null) ? (LoginContext) session.getAttribute("login") : null;
        if (login == null) {
            throw new BusinessException(LOGIN_REQUIRED);
        }

        return ResponseEntity.ok(
                SessionResponse.builder()
                .userId(login.getUserId())
                .storeId(login.getStoreId())
                .userName(login.getUserName())
                .provider(login.getProvider())
                .build()
        );
    }

}
