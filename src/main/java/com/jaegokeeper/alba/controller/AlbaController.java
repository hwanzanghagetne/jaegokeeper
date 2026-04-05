package com.jaegokeeper.alba.controller;

import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.service.AlbaService;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.service.ImageService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alba")
public class AlbaController {

    private final AlbaService albaService;
    private final ImageService imageService;

    @ApiOperation(value = "알바생 전체 조회", notes = "/list?storeId=1 형태로 조회")
    @GetMapping("/list")
    public ResponseEntity<List<AlbaListResponse>> getAllAlbaList(
            @RequestParam(required = false) Integer storeId) {
        return ResponseEntity.ok(albaService.getAllAlbaList(storeId));
    }

    @ApiOperation(value = "알바생 등록", notes = "storeId & albaName & albaPhone & albaStatus")
    @PostMapping("/register")
    public ResponseEntity<Void> saveAlbaRegister(@Valid @ModelAttribute AlbaRegisterRequest req) {
        if (req.getFile() != null && !req.getFile().isEmpty()) {
            ImageInfoDTO imageDto = new ImageInfoDTO();
            imageDto.setFile(req.getFile());
            int imageId = imageService.uploadImg(imageDto);
            req.setImageId(imageId);
        }
        albaService.saveAlbaRegister(req);
        return ResponseEntity.status(201).build();
    }

    @ApiOperation(value = "알바생 상세 조회")
    @GetMapping("/detail/{albaId}")
    public ResponseEntity<AlbaDetailResponse> getAlbaById(@PathVariable int albaId) {
        return ResponseEntity.ok(albaService.getAlbaById(albaId));
    }

    @ApiOperation(value = "알바생 수정")
    @PutMapping("/edit/{albaId}")
    public ResponseEntity<Void> updateById(@PathVariable int albaId, @RequestBody AlbaUpdateRequest req) {
        req.setAlbaId(albaId);
        albaService.updateAlba(req);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "알바생 삭제")
    @DeleteMapping("/delete/{albaId}")
    public ResponseEntity<Void> deleteById(@PathVariable int albaId) {
        albaService.deleteAlba(albaId);
        return ResponseEntity.noContent().build();
    }
}
