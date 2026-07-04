package com.jaegokeeper.auth.dto;

import com.jaegokeeper.auth.utils.SocialProfile;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PendingSocialSignup {
    private final String token;
    private final String provider;
    private final String providerUid;
    private final String email;
    private final String displayName;
    private final boolean emailVerified;
    private final LocalDateTime expiresAt;

    public PendingSocialSignup(String token, String provider, SocialProfile profile, LocalDateTime expiresAt) {
        this.token = token;
        this.provider = provider;
        this.providerUid = profile.getProviderUid();
        this.email = profile.getEmail();
        this.displayName = profile.getDisplayName();
        this.emailVerified = profile.isEmailVerified();
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
