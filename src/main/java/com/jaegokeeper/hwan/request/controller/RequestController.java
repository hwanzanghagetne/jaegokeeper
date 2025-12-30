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
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    // 요청 리스트
    @GetMapping
    public ResponseEntity<PageResponseDTO<RequestListDTO>> getRequests(
            @Valid @ModelAttribute RequestPageRequestDTO dto
    ) {
        return ResponseEntity.ok(
                requestService.getRequestList(dto)
        );
    }

    //요청 생성
    @PostMapping
    public ResponseEntity<?> createRequests(
            @RequestParam Integer storeId,
            @Valid @RequestBody RequestCreateBatchRequestDTO dto
    ) {

        int createdCount = requestService.createRequest(storeId, dto);
        return ResponseEntity.status(201).body(createdCount);
    }

    //요청 등록시 알바 목록
    @GetMapping("/albas")
    public ResponseEntity<List<AlbaOptionDTO>> getAlbaOptions(@RequestParam Integer storeId) {
        List<AlbaOptionDTO> albaOptionsForRequest = requestService.findAlbaOptionsForRequest(storeId);
        return ResponseEntity.ok(albaOptionsForRequest);
    }

    //삭제
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(@RequestParam Integer storeId,
                                              @PathVariable Integer requestId) {
        requestService.deleteRequest(storeId, requestId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{requestId}")
    public ResponseEntity<Void> updateRequest(
            @RequestParam Integer storeId,
            @PathVariable Integer requestId,
            @Valid @RequestBody RequestUpdateRequestDTO dto
    ) {

        requestService.updateRequest(storeId, requestId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{requestId}/status")
    public ResponseEntity<Void> updateRequestStatus(
            @RequestParam Integer storeId,
            @PathVariable Integer requestId,
            @Valid @RequestBody RequestStatusUpdateRequestDTO dto

    ) {
        requestService.updateRequestStatus(storeId, requestId, dto);
        return ResponseEntity.noContent().build();
    }
}
