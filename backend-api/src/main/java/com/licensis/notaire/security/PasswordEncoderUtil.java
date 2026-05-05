package com.licensis.notaire.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Password encryption utility using BCrypt
 * Replaces deprecated MD5 hashing with industry-standard BCrypt
 * Uses Spring Security's BCryptPasswordEncoder for consistency
 */
@Component
public class PasswordEncoderUtil {
    
    private static final int STRENGTH = 12; // BCrypt strength factor (4-31)
    private final PasswordEncoder encoder;

    public PasswordEncoderUtil() {
        this.encoder = new BCryptPasswordEncoder(STRENGTH);
    }

    /**
     * Encode a password using BCrypt
     *
     * @param rawPassword The raw password to encode
     * @return The BCrypt-encoded password hash
     */
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(rawPassword);
    }

    /**
     * Verify a raw password against a BCrypt hash
     *
     * @param rawPassword The raw password to verify
     * @param encodedPassword The BCrypt hash to verify against
     * @return true if the password matches, false otherwise
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Check if a password hash is in BCrypt format
     *
     * @param hash The hash to check
     * @return true if the hash is a BCrypt hash
     */
    public boolean isBCryptHash(String hash) {
        return hash != null && hash.matches("\\$2[aby]\\$\\d{2}\\$.{53}");
    }
}
