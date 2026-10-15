package com.licensis.notaire.adapter.out.persistence.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.user.PasswordHasherPort;

/**
 * Wraps Spring {@link PasswordEncoder} for BCrypt-only credential checks (#1731 / #1762).
 */
@Component
public class PasswordHasherAdapter implements PasswordHasherPort {

    private final PasswordEncoder passwordEncoder;

    public PasswordHasherAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean passwordMatches(String rawPassword, String storedHash) {
        if (!isBcryptHash(storedHash)) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, storedHash);
    }

    @Override
    public boolean isBcryptHash(String hash) {
        return hash != null && hash.startsWith("$2");
    }
}
