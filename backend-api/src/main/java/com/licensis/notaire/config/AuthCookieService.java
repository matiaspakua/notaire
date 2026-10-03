package com.licensis.notaire.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Builds the HttpOnly JWT session cookie used by browser clients (issue #1051).
 * Cookie name must not collide with UX markers {@code notaire-auth-status} /
 * {@code notaire-auth-role} from #1052.
 */
@Service
public class AuthCookieService {

    public static final String DEFAULT_COOKIE_NAME = "notaire-auth-token";

    @Value("${auth.cookie.name:notaire-auth-token}")
    private String cookieName;

    @Value("${auth.cookie.secure:false}")
    private boolean secure;

    @Value("${auth.cookie.max-age-seconds:86400}")
    private long maxAgeSeconds;

    public String cookieName() {
        return cookieName;
    }

    public ResponseCookie createSessionCookie(String jwt) {
        return baseBuilder(jwt)
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }

    public ResponseCookie clearSessionCookie() {
        return baseBuilder("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseBuilder(String value) {
        return ResponseCookie.from(cookieName, value == null ? "" : value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/");
    }
}
