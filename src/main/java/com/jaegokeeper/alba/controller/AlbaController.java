package com.jaegokeeper.alba.controller;

import com.jaegokeeper.alba.dto.AlbaDetailDto;
import com.jaegokeeper.alba.dto.AlbaListDto;
import com.jaegokeeper.alba.dto.AlbaRegisterDto;
import com.jaegokeeper.alba.service.AlbaService;
import com.jaegokeeper.image.service.ImageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/alba")
public class AlbaController {

    private final AlbaService albaService;
    private final ImageService imageService;
    public AlbaController(AlbaService albaService, ImageService imageService) {
        this.albaService = albaService;
        this.imageService = imageService;
    }

    @ApiOperation(value = "알바생 전체 조회 & 스토어 별로 조회하는 페이지", notes = "/list?storeId=1 형태로 조회")
    @GetMapping("/list")
    public ResponseEntity<List<AlbaListDto>> getAllAlbaList(
            @RequestParam(required = false) Integer storeId) {
        List<AlbaListDto> items = albaService.getAllAlbaList(storeId);
        return ResponseEntity.ok(items);
    }

    @ApiOperation(value = "알바생 등록 페이지", notes = "storeId & albaName & albaPhone & albaStatus")
    @PostMapping("/register")
    public ResponseEntity<AlbaRegisterDto> saveAlbaRegister(@Valid @ModelAttribute AlbaRegisterDto albaRegisterDto, @ApiIgnore HttpSession session) throws Exception {
        Integer imageId = null;
        if (albaRegisterDto.getFile() != null || !albaRegisterDto.getFile().isEmpty()) {
            imageId = imageService.uploadImg(albaRegisterDto);
            albaRegisterDto.setImageId(imageId);
        }
        albaService.saveAlbaRegister(albaRegisterDto);
        return ResponseEntity.ok(albaRegisterDto);
    }

    @ApiOperation(value = "알바생 상세 페이지 조회", notes = "detail/8 형태로 조회")
    @GetMapping("/detail/{albaId}")
    public ResponseEntity<AlbaDetailDto> getAlbaById(@PathVariable int albaId) {
        return ResponseEntity.ok(albaService.getAlbaById(albaId));
    }

    @ApiOperation(value = "알바생 수정 페이지", notes = "/edit/{albaId}")
    @PutMapping("/edit/{albaId}")
    public ResponseEntity<AlbaDetailDto> updateById(@PathVariable int albaId, @RequestBody AlbaDetailDto albaDetailDto) {
        albaDetailDto.setAlbaId(albaId);
        albaService.updateAlba(albaDetailDto);
        return ResponseEntity.ok(albaDetailDto);
    }

    @ApiOperation(value = "알바생 삭제 페이지", notes = "/delete/{albaId}")
    @DeleteMapping("/delete/{albaId}")
    public ResponseEntity<Void> deleteById(@PathVariable("albaId") int albaId) {
        albaService.deleteAlba(albaId);
        return ResponseEntity.noContent().build();
    }
}
