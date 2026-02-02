package com.jaegokeeper.auth.controller;

import com.jaegokeeper.auth.dto.*;
import com.jaegokeeper.auth.service.SessionService;
import com.jaegokeeper.auth.service.SocialService;
import com.jaegokeeper.common.api.ApiResponse;
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

    @RequestMapping(value = "/complete",
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