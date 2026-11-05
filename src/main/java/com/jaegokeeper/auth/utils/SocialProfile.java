package com.jaegokeeper.auth.utils;

public class SocialProfile {
    private final String providerUid;
    private final String displayName;
    private final String email;
    private final boolean emailVerified;

    public SocialProfile(String providerUid, String displayName, String email, boolean emailVerified) {
        this.providerUid = providerUid;
        this.displayName = displayName;
        this.email = email;
        this.emailVerified = emailVerified;
    }
    public String getProviderUid() { return providerUid; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public boolean isEmailVerified() { return emailVerified; }
}
