package com.jaegokeeper.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "홈")
@RestController
@RequestMapping("/")
public class HomeController {

    @ApiOperation(value = "홈 페이지", notes = "JaegoKeeper 메인 페이지를 반환합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "성공"),
            @ApiResponse(code = 500, message = "서버 오류")
    })
    @GetMapping("/")
    public String home() {
        return "JaegoKeeper";
    }
}
