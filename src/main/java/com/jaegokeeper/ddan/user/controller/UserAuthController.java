package com.jaegokeeper.ddan.user.controller;

import com.jaegokeeper.ddan.user.mapper.UserAuthMapper;
import com.jaegokeeper.ddan.user.service.SocialAuthService;
import com.jaegokeeper.ddan.user.service.UserService2;
import com.jaegokeeper.ddan.user.utils.SocialVerifier;
import com.jaegokeeper.psj.mapper.StoreMapper2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/auth/login")
public class UserAuthController {

    private final UserAuthMapper userAuthMapper;
    private final SocialAuthService socialAuthService;

    public UserAuthController(UserAuthMapper userAuthMapper, SocialAuthService socialAuthService) {
        this.userAuthMapper = userAuthMapper;
        this.socialAuthService = socialAuthService;
    }


//    @RequestMapping(value="/complete", method=RequestMethod.POST)
//    @ResponseBody
//    public Map<String, Object> loginComplete(
//            @RequestParam("loginId") String loginId,
//            @RequestParam("password") String password,
//            @RequestParam(value="redirectUrl", required=false) String redirectUrl
//    ) throws Exception {
//
//        // 1) 여기서 DB로 유저 검증
//        Integer userId = userAuthMapper.findUserIdByLogin(loginId, password);
//        if (userId == null) {
//            return Map.of("success", false, "message", "아이디/비밀번호가 올바르지 않습니다.");
//        }
//
//        // 2) ticket 발급 (소셜이랑 같은 테이블/DTO 사용)
//        String ticketKey = socialAuthService.issueTicket(userId, redirectUrl);
//
//        return Map.of("success", true, "ticketKey", ticketKey);
//    }

}
