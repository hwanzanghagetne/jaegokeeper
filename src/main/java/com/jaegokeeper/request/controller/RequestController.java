package com.jaegokeeper.request.controller;

import com.jaegokeeper.auth.annotation.LoginUser;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.request.dto.request.RequestCreateBatchRequest;
import com.jaegokeeper.request.dto.request.RequestPageRequest;
import com.jaegokeeper.request.dto.request.RequestStatusUpdateRequest;
import com.jaegokeeper.request.dto.request.RequestUpdateRequest;
import com.jaegokeeper.request.dto.response.RequestDetailResponse;
import com.jaegokeeper.request.dto.response.RequestListResponse;
import com.jaegokeeper.request.service.RequestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Request")
@RestController
@RequestMapping("/stores/{storeId}/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @ApiOperation(value = "요청 목록 조회")
    @GetMapping
    public ResponseEntity<PageResponse<RequestListResponse>> getRequests(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute RequestPageRequest dto,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(requestService.getRequestList(login, storeId, dto));
    }

    @ApiOperation(value = "요청 상세 조회")
    @GetMapping("/{requestId}")
    public ResponseEntity<RequestDetailResponse> getRequestDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer requestId,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(requestService.getRequestDetail(login, storeId, requestId));
    }

    @ApiOperation(value = "요청 생성")
    @PostMapping
    public ResponseEntity<Integer> createRequests(
            @PathVariable Integer storeId,
            @Valid @RequestBody RequestCreateBatchRequest dto,
            @LoginUser LoginContext login) {
        int createdCount = requestService.createRequest(login, storeId, dto);
        return ResponseEntity.status(201).body(createdCount);
    }

    @ApiOperation(value = "요청 삭제")
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Integer storeId,
            @PathVariable Integer requestId,
            @LoginUser LoginContext login) {
        requestService.softDeleteRequest(login, storeId, requestId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "요청 수정")
    @PutMapping("/{requestId}")
    public ResponseEntity<Void> updateRequest(
            @PathVariable Integer storeId,
            @PathVariable Integer requestId,
            @Valid @RequestBody RequestUpdateRequest dto,
            @LoginUser LoginContext login) {
        requestService.updateRequest(login, storeId, requestId, dto);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "요청 상태 수정")
    @PatchMapping("/{requestId}/status")
    public ResponseEntity<Void> updateRequestStatus(
            @PathVariable Integer storeId,
            @PathVariable Integer requestId,
            @Valid @RequestBody RequestStatusUpdateRequest dto,
            @LoginUser LoginContext login) {
        requestService.updateRequestStatus(login, storeId, requestId, dto);
        return ResponseEntity.noContent().build();
    }
}
