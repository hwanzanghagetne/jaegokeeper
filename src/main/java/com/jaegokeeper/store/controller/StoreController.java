package com.jaegokeeper.store.controller;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.store.dto.StoreDto;
import com.jaegokeeper.store.service.StoreService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/store")
public class StoreController {

    private final StoreService storeService;
    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @ApiOperation(value = "store 정보 수정", notes = "/update/{storeId}")
    @PutMapping("/update/{storeId}")
    public ResponseEntity<StoreDto> updateStore(
            @PathVariable int storeId,
            @RequestBody StoreDto storeDto,
            HttpSession session) {

        LoginContext login = (LoginContext) session.getAttribute("login");
        if (login == null || login.getStoreId() != storeId) {
            return ResponseEntity.status(403).build();
        }

        storeDto.setStoreId(storeId);
        storeService.updateStore(storeDto);
        return ResponseEntity.ok(storeDto);
    }
}
