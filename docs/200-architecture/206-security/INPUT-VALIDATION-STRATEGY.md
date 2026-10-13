# Input Validation Strategy

This document defines the validation rules, XSS prevention techniques, CSRF protection, and testing strategy for the Notaire API.

## Validation layers

Notaire enforces validation at two layers:

1. **Client-side (Swing)** — UI field constraints for UX only (not security).
2. **Server-side (Spring Boot controller)** — authoritative validation; all inputs must pass here before reaching the database.

## Standard validation annotations

Use `jakarta.validation` annotations on request DTOs or `@RequestBody` records:

```java
public record UserRequest(
    @NotBlank String name,
    @Size(min = 0, max = 255) String password,
    @NotBlank String type,
    boolean active
) {}
```

Activate with `@Valid` on the parameter:

```java
@PostMapping
public ResponseEntity<?> create(@Valid @RequestBody UserRequest request) { ... }
```

### Annotation reference by field type

| Field type | Annotations |
|-----------|-------------|
| Required string | `@NotBlank` |
| Optional string | `@Size(max = 255)` |
| Email | `@Email` |
| Integer ID | `@Min(1)` |
| Date | `@NotNull`, `@PastOrPresent` |
| Boolean flag | (no annotation needed) |
| File size | handled in `@RequestParam` + `MultipartFile` check |

## XSS prevention

Notaire does not render user input as HTML (the UI is Swing + REST JSON). There is no server-side HTML template engine. XSS risk exists only in:

1. **Log entries** — use parameterized SLF4J calls (`log.warn("msg {}", value)`), never string concatenation.
2. **Next.js frontend** — React's default JSX escaping handles XSS for rendered user data. Never use `dangerouslySetInnerHTML` with user data.
3. **PDF reports** — `PdfBoxReportRenderer` writes field values as PDF text operands only (no markup, scripts, form fields, or attachments). Control and bidirectional-override characters become spaces. User/domain text uses embedded Liberation Sans; Spanish/English fixtures are not replaced with `?`. Glyphs the font cannot encode still become `?` (#567, #1593). Logo and font bytes come only from immutable classpath assets.

For any field that might be reflected back, apply:
```java
String safe = HtmlUtils.htmlEscape(userInput);  // Spring's HtmlUtils
```

## CSRF protection

CSRF attacks target state-changing requests from authenticated sessions. Notaire's threat model:

- **Browser sessions** — HttpOnly JWT cookie via the same-origin Next proxy (#1051). Every API response issues a non-HttpOnly `XSRF-TOKEN` cookie and the client echoes its raw value in the `X-XSRF-TOKEN` header (double-submit, CU78 / #1618). Header-supplied values are resolved verbatim by `CsrfConfigurer.spa()`, so the cookie value is what must be sent, and the nonce is not rotated while the session is unchanged.
- **Bearer-token clients** (Bruno API tests, mobile, server-to-server) — exempt from CSRF: a token the client must attach explicitly cannot be replayed by a third-party site (issue #1620).
- **Login/logout** — exempt so a session can be bootstrapped and torn down.

Configured in `SecurityAndCorsConfig.apiSecurityFilterChain`:

```java
http.csrf(csrf -> csrf
        .spa()  // cookie repository + verbatim header resolution (Spring Security 7)
        .sessionAuthenticationStrategy(KEEP_CSRF_TOKEN_ON_AUTHENTICATION)
        .ignoringRequestMatchers(bearerTokenMatcher(), loginAndLogoutMatcher()));
```

The nonce is not rotated while the session cookie is unchanged: Spring Security's
`CsrfAuthenticationStrategy` replaces the cookie on every authenticated request,
which for a stateless nonce only races a client that reads `document.cookie` and
then sends the request. `SecurityAndCorsConfig` overrides that strategy and
`UserController` appends its own `Set-Cookie` with `HttpServletResponse.addHeader`
(entity headers would replace the token cookie).

Never re-introduce `http.csrf(AbstractHttpConfigurer::disable)` — the Semgrep gate
`security/semgrep/notaire.yml` fails the build on it.

## Rate limiting

Rate limiting is a deployment-level concern. Implement via:

- **Nginx/reverse proxy** — `limit_req_zone` with per-IP sliding window.
- **Spring-level** — Bucket4j library with `RateLimiter` per IP or user.
- **Monitoring** — Prometheus alert `SuspiciousLoginActivity` fires when bad-credential login rate exceeds 30/min.

Priority endpoints for rate limiting:
1. `POST /api/v1/users/login` — brute-force target
2. `GET /api/v1/reports/*` — expensive PDF generation

## File upload security

Currently there are no file upload endpoints. When implemented:

- Validate `Content-Type` and check magic bytes (not just the extension).
- Limit file size via `spring.servlet.multipart.max-file-size`.
- Store files outside the web root; never execute uploaded content.
- Scan with ClamAV or similar before storing.

## Validation testing strategy

```java
// Test missing required field → 400 (UserRequest.name and type are @NotBlank)
mvc.perform(post("/api/v1/users")
    .contentType(APPLICATION_JSON)
    .content("{\"password\":\"pwd\"}"))  // name missing
    .andExpect(status().isBadRequest());

// Oversized username must not crash the login endpoint
mvc.perform(post("/api/v1/users/login")
    .contentType(APPLICATION_JSON)
    .content("{\"name\":\"" + "x".repeat(5000) + "\",\"password\":\"\"}"));

// SQL injection attempt → 200 and valid false
mvc.perform(post("/api/v1/users/login")
    .contentType(APPLICATION_JSON)
    .content("{\"name\":\"' OR '1'='1\",\"password\":\"\"}"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.valid").value(false));
```

See `EdgeCaseBoundaryConditionsTest` for the established test patterns.

## Related documentation

- [`SQL-INJECTION-PREVENTION.md`](SQL-INJECTION-PREVENTION.md)
- [`API-AUTHENTICATION-GUIDE.md`](API-AUTHENTICATION-GUIDE.md)
- [`BACKEND-ERROR-HANDLING-STRATEGY.md`](../203-design/BACKEND-ERROR-HANDLING-STRATEGY.md)
