package com.licensis.notaire.application.port.out.user;

/**
 * Password hashing for user credentials (issue #1731). BCrypt only — legacy MD5 verify
 * was removed (hard cutoff, issue #1762 / java:S4790).
 */
public interface PasswordHasherPort {

    String encode(String rawPassword);

    /**
     * Checks the submitted password against the stored BCrypt hash.
     * Non-BCrypt stored values (including legacy MD5) never match.
     */
    boolean passwordMatches(String rawPassword, String storedHash);

    boolean isBcryptHash(String hash);
}
