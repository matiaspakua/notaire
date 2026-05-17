package com.licensis.notaire.unit;

import com.licensis.notaire.security.PasswordEncoderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PasswordEncoderUtil unit tests")
class PasswordEncoderUtilTest {

    private PasswordEncoderUtil util;

    @BeforeEach
    void setUp() {
        util = new PasswordEncoderUtil();
    }

    @Test
    @DisplayName("encode should return a BCrypt hash with $2 prefix")
    void encodeShouldReturnBCryptHash() {
        String hash = util.encode("secret");
        assertThat(hash).isNotNull();
        assertThat(hash).startsWith("$2");
        assertThat(util.isBCryptHash(hash)).isTrue();
    }

    @Test
    @DisplayName("encode should produce different hashes for the same password (random salt)")
    void encodeShouldProduceDifferentHashesForSamePassword() {
        String h1 = util.encode("secret");
        String h2 = util.encode("secret");
        assertThat(h1).isNotEqualTo(h2);
    }

    @Test
    @DisplayName("encode should throw IllegalArgumentException for null password")
    void encodeShouldThrowForNull() {
        assertThatThrownBy(() -> util.encode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    @DisplayName("encode should throw IllegalArgumentException for empty password")
    void encodeShouldThrowForEmpty() {
        assertThatThrownBy(() -> util.encode(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    @DisplayName("matches should return true when raw matches hash")
    void matchesShouldReturnTrueWhenRawMatchesHash() {
        String hash = util.encode("p@ssword");
        assertThat(util.matches("p@ssword", hash)).isTrue();
    }

    @Test
    @DisplayName("matches should return false for wrong password")
    void matchesShouldReturnFalseForWrongPassword() {
        String hash = util.encode("p@ssword");
        assertThat(util.matches("wrong", hash)).isFalse();
    }

    @Test
    @DisplayName("matches should return false when either argument is null")
    void matchesShouldReturnFalseForNullInputs() {
        String hash = util.encode("p@ssword");
        assertThat(util.matches(null, hash)).isFalse();
        assertThat(util.matches("p@ssword", null)).isFalse();
        assertThat(util.matches(null, null)).isFalse();
    }

    @Test
    @DisplayName("isBCryptHash should return false for null")
    void isBCryptHashShouldReturnFalseForNull() {
        assertThat(util.isBCryptHash(null)).isFalse();
    }

    @Test
    @DisplayName("isBCryptHash should return false for non-bcrypt strings")
    void isBCryptHashShouldReturnFalseForNonBcrypt() {
        assertThat(util.isBCryptHash("plain-password")).isFalse();
        assertThat(util.isBCryptHash("md5hash1234567890abcdef1234567890")).isFalse();
        assertThat(util.isBCryptHash("$2a$12$tooShort")).isFalse();
    }

    @Test
    @DisplayName("isBCryptHash should return true for a real bcrypt hash")
    void isBCryptHashShouldReturnTrueForRealHash() {
        String hash = util.encode("test");
        assertThat(util.isBCryptHash(hash)).isTrue();
    }
}
