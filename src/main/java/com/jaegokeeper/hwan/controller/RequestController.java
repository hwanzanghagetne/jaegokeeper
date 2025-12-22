package com.jaegokeeper.hwan.controller;

import com.jaegokeeper.hwan.domain.enums.RequestStatus;
import com.jaegokeeper.hwan.domain.enums.RequestType;
import com.jaegokeeper.hwan.dto.PageResponseDTO;
import com.jaegokeeper.hwan.dto.RequestCreateBatchRequestDTO;
import com.jaegokeeper.hwan.dto.RequestListDTO;
import com.jaegokeeper.hwan.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<RequestListDTO>> getRequests(
            @RequestParam Integer storeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RequestType requestType,
            @RequestParam(required = false) RequestStatus requestStatus
    ) {
        return ResponseEntity.ok(
                requestService.getRequestList(storeId, page, size, requestType, requestStatus)
        );
    }

    @PostMapping
    public ResponseEntity<?> createRequests(
            @RequestParam Integer storeId,
            @Valid @RequestBody RequestCreateBatchRequestDTO dto
    ) {

        int createdCount = requestService.createRequest(storeId, dto);
        return ResponseEntity.status(201).body(createdCount);
    }
}
