package com.jaegokeeper.user.controller;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.user.dto.UserUpdateRequest;
import com.jaegokeeper.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;


@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @ApiOperation(value = "유저 정보 수정")
    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUser(
            @PathVariable int userId,
            @RequestBody UserUpdateRequest userDto,
            HttpSession session) {

        LoginContext login = (LoginContext) session.getAttribute("login");
        userService.updateUser(login, userId, userDto);
        return ResponseEntity.noContent().build();
    }
}
