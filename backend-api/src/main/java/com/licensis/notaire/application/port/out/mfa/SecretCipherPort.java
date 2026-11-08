package com.licensis.notaire.application.port.out.mfa;

/** Reversible encryption for secrets at rest (ADR-032). */
public interface SecretCipherPort {

    String encrypt(String plaintext);

    String decrypt(String ciphertext);
}
