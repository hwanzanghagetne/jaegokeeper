package com.jaegokeeper.alba.controller;

import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.service.AlbaService;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.service.ImageService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.LOGIN_REQUIRED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/albas")
public class StoreAlbaController {

    private final AlbaService albaService;
    private final ImageService imageService;

    @ApiOperation(value = "스토어별 알바 목록 조회")
    @GetMapping
    public ResponseEntity<List<AlbaListResponse>> getAlbas(
            @PathVariable int storeId,
            HttpSession session) {
        LoginContext login = requireLogin(session);
        return ResponseEntity.ok(albaService.getAllAlbaList(login, storeId));
    }

    @ApiOperation(value = "스토어별 알바 등록")
    @PostMapping
    public ResponseEntity<Void> createAlba(
            @PathVariable int storeId,
            @Valid @ModelAttribute AlbaRegisterRequest req,
            HttpSession session) {
        LoginContext login = requireLogin(session);
        req.setStoreId(storeId);
        if (req.getFile() != null && !req.getFile().isEmpty()) {
            ImageInfoDTO imageDto = new ImageInfoDTO();
            imageDto.setFile(req.getFile());
            int imageId = imageService.uploadImg(imageDto);
            req.setImageId(imageId);
        }
        albaService.saveAlbaRegister(login, storeId, req);
        return ResponseEntity.status(201).build();
    }

    @ApiOperation(value = "스토어별 알바 상세 조회")
    @GetMapping("/{albaId}")
    public ResponseEntity<AlbaDetailResponse> getAlba(
            @PathVariable int storeId,
            @PathVariable int albaId,
            HttpSession session) {
        LoginContext login = requireLogin(session);
        return ResponseEntity.ok(albaService.getAlbaById(login, storeId, albaId));
    }

    @ApiOperation(value = "스토어별 알바 수정")
    @PutMapping("/{albaId}")
    public ResponseEntity<Void> updateAlba(
            @PathVariable int storeId,
            @PathVariable int albaId,
            @RequestBody AlbaUpdateRequest req,
            HttpSession session) {
        LoginContext login = requireLogin(session);
        req.setAlbaId(albaId);
        albaService.updateAlba(login, storeId, req);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "스토어별 알바 삭제")
    @DeleteMapping("/{albaId}")
    public ResponseEntity<Void> deleteAlba(
            @PathVariable int storeId,
            @PathVariable int albaId,
            HttpSession session) {
        LoginContext login = requireLogin(session);
        albaService.deleteAlba(login, storeId, albaId);
        return ResponseEntity.noContent().build();
    }

    private LoginContext requireLogin(HttpSession session) {
        LoginContext login = (session != null) ? (LoginContext) session.getAttribute("login") : null;
        if (login == null) {
            throw new BusinessException(LOGIN_REQUIRED);
        }
        return login;
    }
}
