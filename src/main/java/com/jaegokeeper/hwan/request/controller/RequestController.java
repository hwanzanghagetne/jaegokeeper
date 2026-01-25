package com.jaegokeeper.hwan.request.controller;

import com.jaegokeeper.hwan.alba.dto.AlbaOptionDTO;
import com.jaegokeeper.hwan.item.dto.PageResponseDTO;
import com.jaegokeeper.hwan.request.dto.*;
import com.jaegokeeper.hwan.request.service.RequestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "Request")
@RestController
@RequestMapping("/stores/{storeId}/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    // 요청 리스트
    @ApiOperation(value = "요청 목록 조회", notes = "필터/페이지 조건으로 요청 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<PageResponseDTO<RequestListDTO>> getRequests(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute RequestPageRequestDTO dto
    ) {
        return ResponseEntity.ok(requestService.getRequestList(storeId,dto));
    }

    //요청 생성
    @ApiOperation(value = "요청 생성", notes = "요청을 여러 건으로 생성합니다.")
    @PostMapping
    public ResponseEntity<Integer> createRequests(
            @PathVariable Integer storeId,
            @Valid @RequestBody RequestCreateBatchRequestDTO dto
    ) {
        int createdCount = requestService.createRequest(storeId, dto);
        return ResponseEntity.status(201).body(createdCount);
    }

    //요청 등록시 알바 목록
    @ApiOperation(value = "요청 등록용 알바 목록", notes = "요청 생성 화면에서 선택할 알바 목록을 조회합니다.")
    @GetMapping("/albas")
    public ResponseEntity<List<AlbaOptionDTO>> getAlbaOptions(
            @PathVariable Integer storeId) {
        return ResponseEntity.ok(requestService.findAlbaOptionsForRequest(storeId));
    }

    // 삭제
    @ApiOperation(value = "요청 삭제", notes = "requestId의 요청을 삭제합니다.")
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Integer storeId,
            @PathVariable Integer requestId)
    {
        requestService.softDeleteRequest(storeId, requestId);
        return ResponseEntity.noContent().build();
    }

    // 수정
    @ApiOperation(value = "요청 수정", notes = "requestId의 요청 정보를 수정합니다.")
    @PostMapping("/{requestId}")
    public ResponseEntity<Void> updateRequest(
            @PathVariable Integer storeId,
            @PathVariable Integer requestId,
            @Valid @RequestBody RequestUpdateRequestDTO dto
    ) {
        requestService.updateRequest(storeId, requestId, dto);
        return ResponseEntity.noContent().build();
    }

    // 상태 수정
    @ApiOperation(value = "요청 상태 수정",notes = "requestId의 요청 상태를 수정합니다.")
    @PostMapping("/{requestId}/status")
    public ResponseEntity<Void> updateRequestStatus(
            @PathVariable Integer storeId,
            @PathVariable Integer requestId,
            @Valid @RequestBody RequestStatusUpdateRequestDTO dto

    ) {
        requestService.updateRequestStatus(storeId, requestId, dto);
        return ResponseEntity.noContent().build();
    }
}
