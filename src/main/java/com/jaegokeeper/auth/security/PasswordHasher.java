package com.jaegokeeper.auth.security;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHasher {
    private PasswordHasher() {}

    public static String hash(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(12));
    }

    public static boolean matches(String raw, String hashed) {
        return hashed != null && !hashed.isBlank() && BCrypt.checkpw(raw, hashed);
    }
}