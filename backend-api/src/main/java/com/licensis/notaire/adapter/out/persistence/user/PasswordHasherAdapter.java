package com.licensis.notaire.adapter.out.persistence.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.user.PasswordHasherPort;

/** Wraps Spring {@link PasswordEncoder} and legacy MD5 verify-only (#554 / #1731). */
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
    public boolean matchesBcrypt(String rawPassword, String storedHash) {
        return passwordEncoder.matches(rawPassword, storedHash);
    }

    @Override
    public boolean isBcryptHash(String hash) {
        return hash != null && hash.startsWith("$2");
    }

    @Override
    public String legacyMd5Hash(String plainText) {
        char[] hexChars = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
        try {
            // NOSONAR java:S4790 — verify-only for pre-BCrypt rows (#554); new passwords use PasswordEncoder.
            MessageDigest msgd = MessageDigest.getInstance("MD5");
            byte[] bytes = msgd.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder md5Hex = new StringBuilder(2 * bytes.length);
            for (byte value : bytes) {
                int lowNibble = value & 0x0f;
                int highNibble = (value & 0xf0) >> 4;
                md5Hex.append(hexChars[highNibble]);
                md5Hex.append(hexChars[lowNibble]);
            }
            return md5Hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 MessageDigest is required for legacy password verification", e);
        }
    }
}
