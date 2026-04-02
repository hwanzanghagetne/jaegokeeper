package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.*;
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
    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        if (userAuthMapper.findUserByEmail(email) != null) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);
        }

        UserDTO toInsert = UserDTO.builder()
                .userMail(email)
                .passHash(PasswordHasher.hash(req.getPassword()))
                .userName(req.getName().trim())
                .emailVerified(false)
                .isActive(true)
                .build();

        try {
            int inserted = userAuthMapper.insertUser(toInsert);
            if (inserted != 1 || toInsert.getUserId() == null) {
                throw new BusinessException(REGISTER_FAILED);
            }
        } catch (Exception e) {
            log.error("[REGISTER_FAILED]", e);
            throw new BusinessException(REGISTER_FAILED);
            // UNIQUE(email) 동시성 충돌까지 커버(실무에서 중요)
            // throw ApiException.conflict("EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다.");
        }

        return AuthResponse.builder()
                .userId(toInsert.getUserId())
                .email(toInsert.getUserMail())
                .name(toInsert.getUserName())
                .build();
    }

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
