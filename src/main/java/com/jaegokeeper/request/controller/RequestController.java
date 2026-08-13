package com.jaegokeeper.request.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.request.dto.request.RequestCreateBatchRequest;
import com.jaegokeeper.request.dto.request.RequestPageRequest;
import com.jaegokeeper.request.dto.request.RequestStatusUpdateRequest;
import com.jaegokeeper.request.dto.request.RequestUpdateRequest;
import com.jaegokeeper.request.dto.response.RequestDetailResponse;
import com.jaegokeeper.request.dto.response.RequestListResponse;
import com.jaegokeeper.request.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "Request")
@RestController
@RequestMapping("/stores/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @Operation(summary = "요청 목록 조회")
    @GetMapping
    public ResponseEntity<PageResponse<RequestListResponse>> getRequests(
            @Valid @ModelAttribute RequestPageRequest dto,
            @AuthenticationPrincipal LoginContext login) {
        return ResponseEntity.ok(requestService.getRequestList(login, dto));
    }

    @Operation(summary = "요청 상세 조회")
    @GetMapping("/{requestId}")
    public ResponseEntity<RequestDetailResponse> getRequestDetail(
            @PathVariable Integer requestId,
            @AuthenticationPrincipal LoginContext login) {
        return ResponseEntity.ok(requestService.getRequestDetail(login, requestId));
    }

    @Operation(summary = "요청 생성")
    @PostMapping
    public ResponseEntity<Integer> createRequests(
            @Valid @RequestBody RequestCreateBatchRequest dto,
            @AuthenticationPrincipal LoginContext login) {
        int createdCount = requestService.createRequest(login, dto);
        return ResponseEntity.status(201).body(createdCount);
    }

    @Operation(summary = "요청 삭제")
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Integer requestId,
            @AuthenticationPrincipal LoginContext login) {
        requestService.softDeleteRequest(login, requestId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "요청 수정")
    @PutMapping("/{requestId}")
    public ResponseEntity<Void> updateRequest(
            @PathVariable Integer requestId,
            @Valid @RequestBody RequestUpdateRequest dto,
            @AuthenticationPrincipal LoginContext login) {
        requestService.updateRequest(login, requestId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "요청 상태 수정")
    @PatchMapping("/{requestId}/status")
    public ResponseEntity<Void> updateRequestStatus(
            @PathVariable Integer requestId,
            @Valid @RequestBody RequestStatusUpdateRequest dto,
            @AuthenticationPrincipal LoginContext login) {
        requestService.updateRequestStatus(login, requestId, dto);
        return ResponseEntity.noContent().build();
    }
}
