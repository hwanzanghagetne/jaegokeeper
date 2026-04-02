package com.jaegokeeper.alba.controller;

import com.jaegokeeper.alba.dto.AlbaDetailDto;
import com.jaegokeeper.alba.dto.AlbaListDto;
import com.jaegokeeper.alba.dto.AlbaRegisterDto;
import com.jaegokeeper.alba.service.AlbaService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.image.service.ImageService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.IMAGE_UPLOAD_FAILED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alba")
public class AlbaController {

    private final AlbaService albaService;
    private final ImageService imageService;

    @ApiOperation(value = "알바생 전체 조회", notes = "/list?storeId=1 형태로 조회")
    @GetMapping("/list")
    public ResponseEntity<List<AlbaListDto>> getAllAlbaList(
            @RequestParam(required = false) Integer storeId) {
        return ResponseEntity.ok(albaService.getAllAlbaList(storeId));
    }

    @ApiOperation(value = "알바생 등록", notes = "storeId & albaName & albaPhone & albaStatus")
    @PostMapping("/register")
    public ResponseEntity<AlbaRegisterDto> saveAlbaRegister(@Valid @ModelAttribute AlbaRegisterDto albaRegisterDto) {
        if (albaRegisterDto.getFile() != null && !albaRegisterDto.getFile().isEmpty()) {
            try {
                Integer imageId = imageService.uploadImg(albaRegisterDto);
                albaRegisterDto.setImageId(imageId);
            } catch (IOException e) {
                throw new BusinessException(IMAGE_UPLOAD_FAILED);
            }
        }
        albaService.saveAlbaRegister(albaRegisterDto);
        return ResponseEntity.ok(albaRegisterDto);
    }

    @ApiOperation(value = "알바생 상세 조회")
    @GetMapping("/detail/{albaId}")
    public ResponseEntity<AlbaDetailDto> getAlbaById(@PathVariable int albaId) {
        return ResponseEntity.ok(albaService.getAlbaById(albaId));
    }

    @ApiOperation(value = "알바생 수정")
    @PutMapping("/edit/{albaId}")
    public ResponseEntity<Void> updateById(@PathVariable int albaId, @RequestBody AlbaDetailDto albaDetailDto) {
        albaDetailDto.setAlbaId(albaId);
        albaService.updateAlba(albaDetailDto);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "알바생 삭제")
    @DeleteMapping("/delete/{albaId}")
    public ResponseEntity<Void> deleteById(@PathVariable int albaId) {
        albaService.deleteAlba(albaId);
        return ResponseEntity.noContent().build();
    }
}
