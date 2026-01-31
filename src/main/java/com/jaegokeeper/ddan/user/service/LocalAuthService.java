package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.*;
import com.jaegokeeper.auth.security.PasswordHasher;
import com.jaegokeeper.common.exception.ApiException;
import com.jaegokeeper.ddan.user.mapper.UserAuthMapper;
import com.jaegokeeper.user.dto.UserRow;
import com.jaegokeeper.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class LocalAuthService {

    private final UserAuthMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        if (userMapper.findByEmail(email) != null) {
            throw ApiException.conflict("EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다.");
        }

        UserRow toInsert = UserRow.builder()
                .email(email)
                .passwordHash(PasswordHasher.hash(req.getPassword()))
                .name(req.getName().trim())
                .emailVerified(false)
                .status("ACTIVE")
                .build();

        try {
            int inserted = userMapper.insertUser(toInsert);
            if (inserted != 1 || toInsert.getUserId() == null) {
                throw ApiException.badRequest("REGISTER_FAILED", "회원가입 처리에 실패했습니다.");
            }
        } catch (Exception e) {
            // UNIQUE(email) 동시성 충돌까지 커버(실무에서 중요)
            throw ApiException.conflict("EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다.");
        }

        return AuthResponse.builder()
                .userId(toInsert.getUserId())
                .email(toInsert.getEmail())
                .name(toInsert.getName())
                .build();
    }

    public AuthResponse login(LoginRequest req, HttpSession session) {
        String email = req.getEmail().trim().toLowerCase();

        UserRow user = userMapper.findByEmail(email);
        if (user == null) {
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (user.getStatus() == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw ApiException.unauthorized("USER_NOT_ACTIVE", "사용할 수 없는 계정 상태입니다.");
        }

        if (!PasswordHasher.matches(req.getPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 세션 로그인
        session.setAttribute("LOGIN_USER_ID", user.getUserId());
        session.setAttribute("LOGIN_EMAIL", user.getEmail());
        session.setAttribute("LOGIN_NAME", user.getName());

        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public void logout(HttpSession session) {
        if (session != null) session.invalidate();
    }
}
