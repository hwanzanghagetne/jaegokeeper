package com.jaegokeeper.user.controller;

import com.jaegokeeper.auth.annotation.LoginUser;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.user.dto.UserDetailResponse;
import com.jaegokeeper.user.dto.UserUpdateRequest;
import com.jaegokeeper.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @ApiOperation(value = "유저 정보 조회")
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> getUser(
            @PathVariable int userId,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(userService.getUserDetail(login, userId));
    }

    @ApiOperation(value = "유저 정보 수정")
    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUser(
            @PathVariable int userId,
            @Valid @RequestBody UserUpdateRequest userDto,
            @LoginUser LoginContext login) {
        userService.updateUser(login, userId, userDto);
        return ResponseEntity.noContent().build();
    }
}
