package com.jaegokeeper.psj.controller;

import com.jaegokeeper.psj.dto.UserDto;
import com.jaegokeeper.psj.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // user 정보 수정
    @ApiOperation(value = "user 정보 수정", notes = "/update/{userId}")
    @PutMapping("/update/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable int userId, @RequestBody UserDto userDto) {
        userDto.setUserId(userId);
        userService.updateUser(userDto);
        return ResponseEntity.ok(userDto);
    }
}
