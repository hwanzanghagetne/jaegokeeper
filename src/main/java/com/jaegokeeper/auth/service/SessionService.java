package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.SessionResponse;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserAuthMapper userAuthMapper;

    public SessionResponse createSession(int userId, String provider, HttpServletRequest request) {
        // 1) 유저 조회 (storeId, provider 포함)
        LoginTarget target = userAuthMapper.findByUserIdForSession(userId);
        if (target == null) {
            throw new BusinessException(USER_NOT_FOUND);
        }

        // 2) 세션 재생성 (세션 고정 방지)
        HttpSession old = request.getSession(false);
        if (old != null) old.invalidate();
        HttpSession session = request.getSession(true);

        session.setAttribute("login", new LoginContext(
                target.getUserId(),
                target.getStoreId(),
                target.getUserName(),
                provider
        ));

        return SessionResponse.builder()
                .userId(target.getUserId())
                .storeId(target.getStoreId())
                .userName(target.getUserName())
                .provider(provider)
                .build();
    }

}
