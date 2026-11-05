package com.jaegokeeper.auth.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PasswordHasher {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(12);

    private PasswordHasher() {}

    public static String hash(String raw) {
        return ENCODER.encode(raw);
    }

    public static boolean matches(String raw, String hashed) {
        return hashed != null && !hashed.isBlank() && ENCODER.matches(raw, hashed);
    }
}