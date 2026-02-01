package com.jaegokeeper.auth.controller;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.TicketDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.service.SocialAuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth/social")
public class SocialAuthController {

    private final SocialAuthService socialAuthService;
    private final UserAuthMapper userAuthMapper;

    public SocialAuthController(SocialAuthService socialAuthService, UserAuthMapper userAuthMapper) {
        this.socialAuthService = socialAuthService;
        this.userAuthMapper = userAuthMapper;
    }

    @RequestMapping(value = "/complete", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> complete(
            @RequestParam("provider") String provider,
            @RequestParam("accessToken") String accessToken,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl
    ) throws Exception {

        String ticketKey = socialAuthService.completeAndIssueTicket(
                provider.toUpperCase(), accessToken, redirectUrl
        );

        Map<String, Object> res = new java.util.HashMap<>();
        res.put("success", true);
        res.put("ticketKey", ticketKey);
        return res;
    }

    @RequestMapping(value = "/consume", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> consume(
            @RequestParam("key") String key,
            HttpServletRequest request
    ) throws Exception {

        Map<String, Object> res = new HashMap<>();

        // 1) 티켓 조회
        TicketDTO ticket = userAuthMapper.findValidTicket(key);
        if (ticket == null) {
            res.put("success", false);
            res.put("reason", "INVALID_TICKET");
            return res;
        }

        // 2) used 처리
        int updated = userAuthMapper.markUsed(key);
        if (updated != 1) {
            res.put("success", false);
            res.put("reason", "ALREADY_USED");
            return res;
        }

        // 3) 유저 조회
        LoginTarget target = userAuthMapper.findByUserIdForSession(ticket.getUserId());
        if (target == null) {
            res.put("success", false);
            res.put("reason", "USER_NOT_FOUND");
            return res;
        }

        // 4) 세션 재생성 (세션 고정 방지)
        HttpSession old = request.getSession(false);
        if (old != null) old.invalidate();
        HttpSession session = request.getSession(true);

        session.setAttribute("login", new LoginContext(
                target.getUserId(),
                target.getStoreId(),
                target.getUserName(),
                "SOCIAL"
        ));

        // 5) 성공 응답
        res.put("success", true);
        res.put("redirectUrl",
                (ticket.getRedirectUrl() != null && !ticket.getRedirectUrl().isEmpty())
                        ? ticket.getRedirectUrl()
                        : "/"
        );
        res.put("userId", target.getUserId());
        res.put("storeId", target.getStoreId());
        res.put("userName", target.getUserName());

        return res;
    }

}