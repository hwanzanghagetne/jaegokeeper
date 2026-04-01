package com.jaegokeeper.user.controller;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.user.dto.UserDto;
import com.jaegokeeper.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiOperation(value = "user 정보 수정", notes = "/update/{userId}")
    @PutMapping("/update/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable int userId,
            @RequestBody UserDto userDto,
            HttpSession session) {

        LoginContext login = (LoginContext) session.getAttribute("login");
        if (login == null || login.getUserId() != userId) {
            return ResponseEntity.status(403).build();
        }

        userDto.setUserId(userId);
        userService.updateUser(userDto);
        return ResponseEntity.ok(userDto);
    }
}
