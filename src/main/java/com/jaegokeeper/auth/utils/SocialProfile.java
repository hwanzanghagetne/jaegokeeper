package com.jaegokeeper.auth.utils;

public class SocialProfile {
    private final String providerUid; // 고유 ID
    private final String displayName; // 옵션
    private final String email;       // 옵션

    public SocialProfile(String providerUid, String displayName, String email) {
        this.providerUid = providerUid;
        this.displayName = displayName;
        this.email = email;
    }
    public String getProviderUid() { return providerUid; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
}
