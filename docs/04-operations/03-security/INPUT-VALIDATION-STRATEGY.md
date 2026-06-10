# Input Validation Strategy

This document defines the validation rules, XSS prevention techniques, CSRF protection, and testing strategy for the Notaire API.

## Validation layers

Notaire enforces validation at two layers:

1. **Client-side (Swing)** — UI field constraints for UX only (not security).
2. **Server-side (Spring Boot controller)** — authoritative validation; all inputs must pass here before reaching the database.

## Standard validation annotations

Use `jakarta.validation` annotations on request DTOs or `@RequestBody` records:

```java
public record UsuarioRequest(
    @NotBlank String nombre,
    @Size(min = 0, max = 255) String contrasenia,
    @NotBlank String tipo,
    boolean activo
) {}
```

Activate with `@Valid` on the parameter:

```java
@PostMapping
public ResponseEntity<?> create(@Valid @RequestBody UsuarioRequest request) { ... }
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
2. **Future web frontend** — if a Next.js/React frontend is added, React's default JSX escaping handles XSS. Never use `dangerouslySetInnerHTML` with user data.
3. **PDF reports** — JasperReports renders user-supplied field values into PDF; ensure field values are sanitized before being passed to JasperFillManager.

For any field that might be reflected back, apply:
```java
String safe = HtmlUtils.htmlEscape(userInput);  // Spring's HtmlUtils
```

## CSRF protection

CSRF attacks target state-changing requests from authenticated sessions. Notaire's threat model:

- **Current** — Stateless REST API with MD5 password login; no session cookie. CSRF does not apply to cookie-less JSON APIs.
- **Future (if session/cookie auth is added)** — Enable Spring Security's CSRF filter and send the `X-CSRF-TOKEN` header from the frontend.

Current Spring Security configuration explicitly disables CSRF:
```java
http.csrf(AbstractHttpConfigurer::disable);  // safe for stateless JWT API
```

## Rate limiting

Rate limiting is a deployment-level concern. Implement via:

- **Nginx/reverse proxy** — `limit_req_zone` with per-IP sliding window.
- **Spring-level** — Bucket4j library with `RateLimiter` per IP or user.
- **Monitoring** — Prometheus alert `SuspiciousLoginActivity` fires when bad-credential login rate exceeds 30/min.

Priority endpoints for rate limiting:
1. `POST /api/v1/usuarios/login` — brute-force target
2. `GET /api/v1/reportes/*` — expensive PDF generation

## File upload security

Currently there are no file upload endpoints. When implemented:

- Validate `Content-Type` and check magic bytes (not just the extension).
- Limit file size via `spring.servlet.multipart.max-file-size`.
- Store files outside the web root; never execute uploaded content.
- Scan with ClamAV or similar before storing.

## Validation testing strategy

```java
// Test missing required field → 400
mvc.perform(post("/api/v1/usuarios")
    .contentType(APPLICATION_JSON)
    .content("{\"contrasenia\":\"pwd\"}"))  // nombre missing
    .andExpect(status().isBadRequest());

// Test oversized field → 400
mvc.perform(post("/api/v1/usuarios/login")
    .contentType(APPLICATION_JSON)
    .content("{\"nombre\":\"" + "x".repeat(5000) + "\",\"contrasenia\":\"\"}"))
    .andExpect(status().isOk());  // login returns 200 with valido:false

// Test SQL injection attempt → no 500
mvc.perform(post("/api/v1/usuarios/login")
    .contentType(APPLICATION_JSON)
    .content("{\"nombre\":\"' OR '1'='1\",\"contrasenia\":\"\"}"))
    .andExpect(status().isOk());
```

See `EdgeCaseBoundaryConditionsTest` for the established test patterns.

## Related documentation

- `docs/04-operations/03-security/SQL-INJECTION-PREVENTION.md`
- `docs/04-operations/03-security/API-AUTHENTICATION-GUIDE.md`
- `docs/05-api/ERROR-HANDLING-STRATEGY.md`
