package com.licensis.notaire.adapter.out.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.mfa.SecretCipherPort;

/** AES-256-GCM; output is Base64(IV || ciphertext+tag) with a fresh 96-bit IV (ADR-032). */
@Component
public class AesGcmSecretCipherAdapter implements SecretCipherPort {

    private static final int KEY_BYTES = 32;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmSecretCipherAdapter(@Value("${notaire.mfa.encryption-key:}") String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            this.key = null;
            return;
        }
        byte[] bytes = decodeKey(base64Key);
        if (bytes.length != KEY_BYTES) {
            throw new IllegalStateException("NOTAIRE_MFA_ENCRYPTION_KEY must be Base64 of exactly 32 bytes");
        }
        this.key = new SecretKeySpec(bytes, "AES");
    }

    @Override
    public String encrypt(String plaintext) {
        requireKey();
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv).put(encrypted).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Secret encryption failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        requireKey();
        try {
            ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(ciphertext));
            byte[] iv = new byte[IV_BYTES];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | RuntimeException e) {
            throw new IllegalStateException("Secret decryption failed", e);
        }
    }

    private void requireKey() {
        if (key == null) {
            throw new IllegalStateException("NOTAIRE_MFA_ENCRYPTION_KEY is not configured");
        }
    }

    private static byte[] decodeKey(String base64Key) {
        try {
            return Base64.getDecoder().decode(base64Key == null ? "" : base64Key.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("NOTAIRE_MFA_ENCRYPTION_KEY is not valid Base64", e);
        }
    }
}
