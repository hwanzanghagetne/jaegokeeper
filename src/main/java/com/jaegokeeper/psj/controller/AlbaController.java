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

    // 알바생 전체 조회
    @GetMapping("/list")
    public ResponseEntity<List<AlbaListDto>> getAllItem(@RequestParam int storeId) {
        List<AlbaListDto> items = albaService.getAllCheck(storeId);
        return  ResponseEntity.ok(items);
    }

    // 알바생 등록 페이지
    @PostMapping("/register")
    public ResponseEntity<AlbaRegisterDto> mapRequest(@Valid @RequestBody AlbaRegisterDto item, HttpSession session) {
        Integer storeId = (Integer) session.getAttribute("storeId");

        // 세션에 없을 때 테스트용
        if (storeId == null) {
            storeId = 1;
        }

        item.setStoreId(storeId);
        albaService.saveAlbaDto(item);
        return ResponseEntity.ok(item);
    }


    // 알바생 상세 페이지
    @GetMapping("/list/{id}")
    public ResponseEntity<AlbaDetailDto> getAlbaById(@PathVariable int id) {
        return ResponseEntity.ok(albaService.getByIdCheck(id));
    }

    // 알바생 수정 페이지
    @PutMapping("/list/{id}")
    public ResponseEntity<AlbaDetailDto> updateById(@PathVariable int id, @RequestBody AlbaDetailDto albaDetailDto) {
        albaDetailDto.setAlbaId(id);
        albaService.updateAlba(albaDetailDto);
        return ResponseEntity.ok(albaDetailDto);
    }

    // 알바생 삭제 페이지
    @DeleteMapping("/list/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") int id) {
        albaService.deleteAlba(id);
        return ResponseEntity.noContent().build();
    }
}
