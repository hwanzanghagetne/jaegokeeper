package com.jaegokeeper.store.controller;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.store.dto.StoreUpdateRequest;
import com.jaegokeeper.store.service.StoreService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import static com.jaegokeeper.exception.ErrorCode.LOGIN_REQUIRED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    @ApiOperation(value = "점포 정보 수정")
    @PutMapping("/{storeId}")
    public ResponseEntity<Void> updateStore(
            @PathVariable int storeId,
            @Valid @RequestBody StoreUpdateRequest req,
            HttpSession session) {

        LoginContext login = requireLogin(session);
        storeService.updateStore(login, storeId, req);
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
