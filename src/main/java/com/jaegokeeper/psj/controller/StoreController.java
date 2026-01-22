package com.jaegokeeper.psj.controller;

import com.jaegokeeper.psj.dto.StoreDto;
import com.jaegokeeper.psj.service.StoreService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/store")
public class StoreController {

    private final StoreService storeService;
    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // store 정보 수정
    @ApiOperation(value = "store 정보 수정", notes = "/update/{storeId}")
    @PutMapping("/update/{storeId}")
    public ResponseEntity<StoreDto> updateStore(@PathVariable int storeId, @RequestBody StoreDto storeDto) {
        storeDto.setStoreId(storeId);
        storeService.updateStore(storeDto);
        return ResponseEntity.ok(storeDto);
    }
}
