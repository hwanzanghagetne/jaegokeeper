package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.dto.PendingSocialSignup;
import com.jaegokeeper.auth.utils.SocialProfile;
import com.jaegokeeper.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.jaegokeeper.exception.ErrorCode.BAD_REQUEST;

@Service
public class PendingSocialSignupService {

    private static final int EXPIRE_MINUTES = 10;

    private final Map<String, PendingSocialSignup> pendingSignups = new ConcurrentHashMap<>();

    public PendingSocialSignup create(String provider, SocialProfile profile) {
        cleanupExpired();
        String token = UUID.randomUUID().toString();
        PendingSocialSignup pending = new PendingSocialSignup(
                token,
                provider,
                profile,
                LocalDateTime.now().plusMinutes(EXPIRE_MINUTES)
        );
        pendingSignups.put(token, pending);
        return pending;
    }

    public PendingSocialSignup consume(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(BAD_REQUEST);
        }

        PendingSocialSignup pending = pendingSignups.remove(token);
        if (pending == null || pending.isExpired()) {
            throw new BusinessException(BAD_REQUEST);
        }
        return pending;
    }

    private void cleanupExpired() {
        pendingSignups.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
