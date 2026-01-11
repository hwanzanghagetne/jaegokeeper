package com.jaegokeeper.hwan.request.controller;

import com.jaegokeeper.hwan.alba.dto.AlbaOptionDTO;
import com.jaegokeeper.hwan.item.dto.PageResponseDTO;
import com.jaegokeeper.hwan.request.dto.*;
import com.jaegokeeper.hwan.request.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/stores/{storeId}/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    // 요청 리스트
    @GetMapping
    public ResponseEntity<PageResponseDTO<RequestListDTO>> getRequests(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute RequestPageRequestDTO dto
    ) {
        return ResponseEntity.ok(requestService.getRequestList(storeId,dto));
    }

    //요청 생성
    @PostMapping
    public ResponseEntity<Integer> createRequests(
            @PathVariable Integer storeId,
            @Valid @RequestBody RequestCreateBatchRequestDTO dto
    ) {
        int createdCount = requestService.createRequest(storeId, dto);
        return ResponseEntity.status(201).body(createdCount);
    }

    //요청 등록시 알바 목록
    @GetMapping("/albas")
    public ResponseEntity<List<AlbaOptionDTO>> getAlbaOptions(
            @PathVariable Integer storeId) {
        return ResponseEntity.ok(requestService.findAlbaOptionsForRequest(storeId));
    }

    //삭제
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Integer storeId,
            @PathVariable Integer requestId)
    {
        requestService.softDeleteRequest(storeId, requestId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{requestId}")
    public ResponseEntity<Void> updateRequest(
            @PathVariable Integer storeId,
            @PathVariable Integer requestId,
            @Valid @RequestBody RequestUpdateRequestDTO dto
    ) {
        requestService.updateRequest(storeId, requestId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{requestId}/status")
    public ResponseEntity<Void> updateRequestStatus(
            @PathVariable Integer storeId,
            @PathVariable Integer requestId,
            @Valid @RequestBody RequestStatusUpdateRequestDTO dto

    ) {
        requestService.updateRequestStatus(storeId, requestId, dto);
        return ResponseEntity.noContent().build();
    }
}
