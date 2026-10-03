package com.licensis.notaire.unit;

import com.licensis.notaire.config.AuthCookieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthCookieService — HttpOnly JWT cookie attributes (issue #1051)")
class AuthCookieServiceTest {

    private AuthCookieService service;

    @BeforeEach
    void setUp() {
        service = new AuthCookieService();
        ReflectionTestUtils.setField(service, "cookieName", "notaire-auth-token");
        ReflectionTestUtils.setField(service, "secure", true);
        ReflectionTestUtils.setField(service, "maxAgeSeconds", 86400L);
    }

    @Test
    @DisplayName("shouldCreateCookie with HttpOnly Secure SameSite=Lax Path=/")
    void shouldCreateSessionCookieWithRequiredAttributes() {
        ResponseCookie cookie = service.createSessionCookie("jwt-value");

        assertThat(cookie.getName()).isEqualTo("notaire-auth-token");
        assertThat(cookie.getValue()).isEqualTo("jwt-value");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(86400L);
    }

    @Test
    @DisplayName("shouldClearCookie with Max-Age=0")
    void shouldClearSessionCookie() {
        ResponseCookie cookie = service.clearSessionCookie();

        assertThat(cookie.getName()).isEqualTo("notaire-auth-token");
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge().getSeconds()).isZero();
        assertThat(cookie.getPath()).isEqualTo("/");
    }
}
