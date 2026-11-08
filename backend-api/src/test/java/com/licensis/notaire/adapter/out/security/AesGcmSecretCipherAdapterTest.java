package com.licensis.notaire.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AES-256-GCM secret cipher (CU78, ADR-032)")
class AesGcmSecretCipherAdapterTest {

    private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    @DisplayName("round-trips a secret and never stores it in clear")
    void shouldRoundTrip() {
        AesGcmSecretCipherAdapter cipher = new AesGcmSecretCipherAdapter(KEY);

        String encrypted = cipher.encrypt("JBSWY3DPEHPK3PXP");

        assertThat(encrypted).doesNotContain("JBSWY3DPEHPK3PXP");
        assertThat(cipher.decrypt(encrypted)).isEqualTo("JBSWY3DPEHPK3PXP");
    }

    @Test
    @DisplayName("uses a fresh IV per encryption")
    void shouldUseFreshIv() {
        AesGcmSecretCipherAdapter cipher = new AesGcmSecretCipherAdapter(KEY);

        assertThat(cipher.encrypt("S")).isNotEqualTo(cipher.encrypt("S"));
    }

    @Test
    @DisplayName("detects tampering")
    void shouldDetectTampering() {
        AesGcmSecretCipherAdapter cipher = new AesGcmSecretCipherAdapter(KEY);
        byte[] raw = Base64.getDecoder().decode(cipher.encrypt("SECRET"));
        raw[raw.length - 1] ^= 1;

        String tampered = Base64.getEncoder().encodeToString(raw);

        assertThatThrownBy(() -> cipher.decrypt(tampered)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("refuses a malformed key at startup and an absent key on use")
    void shouldRefuseBadKey() {
        assertThatThrownBy(() -> new AesGcmSecretCipherAdapter("").encrypt("S"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AesGcmSecretCipherAdapter("not base64!")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AesGcmSecretCipherAdapter(Base64.getEncoder().encodeToString(new byte[16])))
                .isInstanceOf(IllegalStateException.class);
    }
}
