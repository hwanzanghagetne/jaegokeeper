package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.SessionResponse;
import com.jaegokeeper.auth.dto.TicketDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserAuthMapper userAuthMapper;

    @Transactional
    public SessionResponse handleSessionAuth(
            String ticketKey, HttpServletRequest request
    ) {
        // 1) 티켓 조회
        TicketDTO ticket = userAuthMapper.findValidTicket(ticketKey);
        if (ticket == null) {
            throw new BusinessException(INVALID_TICKET);
        }

        // 2) used 처리
        int updated = userAuthMapper.markUsed(ticketKey);
        if (updated != 1) {
            throw new BusinessException(INVALID_OR_USED_TICKET);
        }

        // 3) 유저 조회
        LoginTarget target = userAuthMapper.findByUserIdForSession(ticket.getUserId());
        if (target == null) {
            throw new BusinessException(USER_NOT_FOUND);
        }

        // 4) 세션 재생성(세션 고정 방지)
        HttpSession old = request.getSession(false);
        if (old != null) old.invalidate();
        HttpSession session = request.getSession(true);

        session.setAttribute("login", new LoginContext(
                target.getUserId(),
                target.getStoreId(),
                target.getUserName(),
                target.getProvider()
        ));

        String redirectUrl = ticket.getRedirectUrl();
        if (redirectUrl == null || !redirectUrl.startsWith("/")) {
            redirectUrl = "/";
        }

        return SessionResponse.builder()
                .redirectUrl(redirectUrl)
                .userId(target.getUserId())
                .storeId(target.getStoreId())
                .userName(target.getUserName())
                .build();
    }

}
