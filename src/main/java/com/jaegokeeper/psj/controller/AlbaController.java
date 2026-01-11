package com.jaegokeeper.psj.controller;

import com.jaegokeeper.psj.dto.AlbaDetailDto;
import com.jaegokeeper.psj.dto.AlbaListDto;
import com.jaegokeeper.psj.dto.AlbaRegisterDto;
import com.jaegokeeper.psj.service.AlbaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/alba")
public class AlbaController {

    private final AlbaService albaService;
    public AlbaController(AlbaService albaService) {
        this.albaService = albaService;
    }

    // 알바생 전체 조회 & 스토어 별로 조회하는 페이지
    // list?storeId=1 형태로 조회
    @GetMapping("/list")
    public ResponseEntity<List<AlbaListDto>> getAllAlbaList(
            @RequestParam(required = false) Integer storeId) {
        List<AlbaListDto> items = albaService.getAllAlbaList(storeId);
        return  ResponseEntity.ok(items);
    }

    // 알바생 등록 페이지
    // storeId & albaName & albaPhone & albaStatus & workDate & workStatus를 POST
    @PostMapping("/register")
    public ResponseEntity<AlbaRegisterDto> saveAlbaRegister(@Valid @RequestBody AlbaRegisterDto albaRegisterDto, HttpSession session) {
//        Integer storeId = (Integer) session.getAttribute("storeId");
//        albaRegisterDto.setStoreId(storeId);
        albaService.saveAlbaRegister(albaRegisterDto);
        return ResponseEntity.ok(albaRegisterDto);
    }

    // 알바생 상세페이지 조회
    // detail/8 형태로 조회
    @GetMapping("/detail/{albaId}")
    public ResponseEntity<AlbaDetailDto> getAlbaById(@PathVariable int albaId) {
        return ResponseEntity.ok(albaService.getAlbaById(albaId));
    }

    // 알바생 수정 페이지
    @PutMapping("/detail/{albaId}")
    public ResponseEntity<AlbaDetailDto> updateById(@PathVariable int albaId, @RequestBody AlbaDetailDto albaDetailDto) {
        albaDetailDto.setAlbaId(albaId);
        albaService.updateAlba(albaDetailDto);
        return ResponseEntity.ok(albaDetailDto);
    }

    // 알바생 삭제 페이지
    @DeleteMapping("/detail/{albaId}")
    public ResponseEntity<Void> deleteById(@PathVariable("albaId") int albaId) {
        albaService.deleteAlba(albaId);
        return ResponseEntity.noContent().build();
    }
}
