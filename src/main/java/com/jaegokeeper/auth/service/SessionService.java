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
        LoginTarget target = userAuthMapper.findByUserIdForSession(userId);
        if (target == null) {
            throw new BusinessException(USER_NOT_FOUND);
        }

        HttpSession old = request.getSession(false);
        if (old != null) old.invalidate();
        request.getSession(true);

        LoginContext loginContext = new LoginContext(
                target.getUserId(),
                target.getStoreId(),
                target.getUserName(),
                provider
        );

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(loginContext, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return SessionResponse.builder()
                .userId(target.getUserId())
                .storeId(target.getStoreId())
                .userName(target.getUserName())
                .provider(provider)
                .build();
    }

}
