package com.licensis.notaire.domain.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Base32 codec (RFC 4648) for authenticator secrets")
class Base32CodecTest {

    @Test
    void shouldEncodeRfcVector() {
        assertThat(Base32Codec.encode("foobar".getBytes(StandardCharsets.US_ASCII))).isEqualTo("MZXW6YTBOI");
    }

    @Test
    void shouldDecodeRoundTrip() {
        byte[] bytes = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

        assertThat(Base32Codec.decode(Base32Codec.encode(bytes))).isEqualTo(bytes);
    }
}
