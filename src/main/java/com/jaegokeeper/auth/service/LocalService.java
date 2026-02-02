package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.*;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.auth.utils.PasswordHasher;
import com.jaegokeeper.common.api.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LocalService {

    private final UserAuthMapper userAuthMapper;

    @Transactional
    public AuthResponse register(RegisterRequest req) throws Exception {
        String email = req.getEmail().trim().toLowerCase();

        if (userAuthMapper.findUserByEmail(email) != null) {
            throw ApiException.conflict("EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다.");
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
                throw ApiException.badRequest("REGISTER_FAILED", "회원가입 처리에 실패했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw ApiException.badRequest("REGISTER_FAILED", "회원가입 처리에 실패했습니다.");
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
    public String loginAndIssueTicket(LoginRequest req, String redirectUrl) throws Exception {
        String email = req.getEmail().trim().toLowerCase();

        UserDTO user = userAuthMapper.findUserByEmail(email);
        if (user == null) {
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw ApiException.unauthorized("USER_NOT_ACTIVE", "사용할 수 없는 계정 상태입니다.");
        }

        if (!PasswordHasher.matches(req.getPassword(), user.getPassHash())) {
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (redirectUrl == null) {
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
            throw ApiException.badRequest("TICKET_ISSUE_FAILED", "티켓 발급에 실패했습니다.");
        }

        return ticketKey;
    }

}
