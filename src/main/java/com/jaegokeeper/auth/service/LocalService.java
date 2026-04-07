package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.LoginRequest;
import com.jaegokeeper.auth.dto.TicketDTO;
import com.jaegokeeper.auth.dto.UserDTO;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.utils.PasswordHasher;
import com.jaegokeeper.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.jaegokeeper.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalService {

    private final UserAuthMapper userAuthMapper;

    @Transactional
    public String loginAndIssueTicket(LoginRequest req, String redirectUrl) {
        String email = req.getEmail().trim().toLowerCase();

        UserDTO user = userAuthMapper.findUserByEmail(email);
        if (user == null) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BusinessException(USER_NOT_ACTIVE);
        }

        if (!PasswordHasher.matches(req.getPassword(), user.getPassHash())) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        if (redirectUrl == null || !redirectUrl.startsWith("/")) {
            redirectUrl = "/";
        }

        // ticket 발급
        String ticketKey = UUID.randomUUID().toString();

        TicketDTO ticket = new TicketDTO();
            ticket.setTicketKey(ticketKey);
            ticket.setUserId(user.getUserId());
            ticket.setRedirectUrl(redirectUrl);
            ticket.setExpiresAt(new Date(System.currentTimeMillis() + 5 * 60 * 1000)); // 5분 예시

        int inserted = userAuthMapper.insertTicket(ticket);
        if (inserted != 1) {
            throw new BusinessException(TICKET_ISSUE_FAILED);
        }

        return ticketKey;
    }

}
