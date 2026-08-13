package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.dto.LoginTarget;
import com.jaegokeeper.auth.dto.SessionResponse;
import com.jaegokeeper.auth.mapper.UserAuthMapper;
import com.jaegokeeper.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserAuthMapper userAuthMapper;
    private final SecurityContextRepository securityContextRepository;

    public SessionResponse createSession(int userId, String provider, HttpServletRequest request, HttpServletResponse response) {
        // 1) 유저 조회 (storeId, provider 포함)
        LoginTarget target = userAuthMapper.findByUserIdForSession(userId);
        if (target == null) {
            throw new BusinessException(USER_NOT_FOUND);
        }

        // 2) 세션 재생성 (세션 고정 방지)
        HttpSession old = request.getSession(false);
        if (old != null) old.invalidate();
        request.getSession(true);

        LoginContext loginContext = new LoginContext(
                target.getUserId(),
                target.getStoreId(),
                target.getUserName(),
                provider
        );

        // 3) 인증 정보를 SecurityContext에 등록
        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        // 현재 요청 스레드에 즉시 반영
        SecurityContextHolder.setContext(context);
        // 다음 요청부터 쓸 수 있도록 세션에 저장 (SecurityContextHolderFilter는 자동 저장을 안 해줌)
        securityContextRepository.saveContext(context, request, response);

        return SessionResponse.builder()
                .userId(target.getUserId())
                .storeId(target.getStoreId())
                .userName(target.getUserName())
                .provider(provider)
                .build();
    }

}
