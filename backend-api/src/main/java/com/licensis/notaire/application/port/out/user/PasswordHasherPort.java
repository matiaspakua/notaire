package com.licensis.notaire.application.port.out.user;

/**
 * Password hashing for user credentials (issue #1731). BCrypt for new hashes; legacy MD5 verify-only
 * for pre-migration rows (#554).
 */
public interface PasswordHasherPort {

    String encode(String rawPassword);

    boolean matchesBcrypt(String rawPassword, String storedHash);

    boolean isBcryptHash(String hash);

    /** Verify-only for pre-BCrypt rows; never used to create new hashes. */
    String legacyMd5Hash(String rawPassword);
}
