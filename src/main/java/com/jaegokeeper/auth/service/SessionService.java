package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.SessionResponse;
import com.jaegokeeper.auth.dto.TicketDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.common.api.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserAuthMapper userAuthMapper;

    @Transactional
    public SessionResponse handleSessionAuth(
            String ticketKey, String provider, HttpServletRequest request
    ) throws Exception {
        // 1) 티켓 조회
        TicketDTO ticket = userAuthMapper.findValidTicket(ticketKey);
        if (ticket == null) {
            throw ApiException.unauthorized("INVALID_TICKET", "티켓이 존재하지 않습니다.");
        }

        // 2) used 처리
        int updated = userAuthMapper.markUsed(ticketKey);
        if (updated != 1) {
            throw ApiException.unauthorized("INVALID_OR_USED_TICKET", "티켓이 유효하지 않거나 이미 사용되었습니다.");
        }

        // 3) 유저 조회
        LoginTarget target = userAuthMapper.findByUserIdForSession(ticket.getUserId());
        if (target == null) {
            throw ApiException.unauthorized("USER_NOT_FOUND", "유저를 찾을 수 없습니다.");
        }

        // 4) 세션 재생성(세션 고정 방지)
        HttpSession old = request.getSession(false);
        if (old != null) old.invalidate();
        HttpSession session = request.getSession(true);

        session.setAttribute("login", new LoginContext(
                target.getUserId(),
                target.getStoreId(),
                target.getUserName(),
                provider
        ));

        String redirectUrl =
                (ticket.getRedirectUrl() != null && !ticket.getRedirectUrl().isEmpty())
                        ? ticket.getRedirectUrl()
                        : "/";

        return SessionResponse.builder()
                .redirectUrl(redirectUrl)
                .userId(target.getUserId())
                .storeId(target.getStoreId())
                .userName(target.getUserName())
                .build();
    }

}
