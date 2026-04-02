package com.jaegokeeper.store.controller;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.store.dto.StoreDto;
import com.jaegokeeper.store.service.StoreService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import static com.jaegokeeper.exception.ErrorCode.FORBIDDEN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    @ApiOperation(value = "점포 정보 수정")
    @PutMapping("/{storeId}")
    public ResponseEntity<Void> updateStore(
            @PathVariable int storeId,
            @RequestBody StoreDto storeDto,
            HttpSession session) {

        LoginContext login = (LoginContext) session.getAttribute("login");
        if (login.getStoreId() != storeId) {
            throw new BusinessException(FORBIDDEN);
        }

        storeDto.setStoreId(storeId);
        storeService.updateStore(storeDto);
        return ResponseEntity.noContent().build();
    }
}
