package com.licensis.notaire.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenService {

    private static final int MIN_SECRET_LENGTH = 32;
    private static final String INSECURE_DEFAULT_SECRET = "notaire-default-secret-key-change-in-production-!!";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    @PostConstruct
    public void validateSecret() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "jwt.secret no está configurado. Definí una clave secreta propia (variable de entorno "
                            + "JWT_SECRET) antes de iniciar la aplicación.");
        }
        if (INSECURE_DEFAULT_SECRET.equals(secretKey)) {
            throw new IllegalStateException(
                    "jwt.secret usa el valor por defecto inseguro incluido en el repositorio. "
                            + "Definí una clave secreta propia (variable de entorno JWT_SECRET).");
        }
        if (secretKey.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "jwt.secret es demasiado corto para HS256: se requieren al menos " + MIN_SECRET_LENGTH
                            + " bytes.");
        }
    }

    public String generateToken(String username) {
        SecretKey key = signingKey();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Claims c = claims(token);
            return c.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
