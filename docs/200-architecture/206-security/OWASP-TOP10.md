# OWASP Top 10 — Notaire mapping

Advances issue **#267** (CU78). A human security review is still required
before that issue closes. Verification names a file or a workflow, not a
percentage.

| ID | Risk | Control | Verification | Gap |
|----|------|---------|--------------|-----|
| A01 Broken access control | `ROLE_ADMIN` on admin paths and admin writes. Frontend admin routes redirect | `SecurityAndCorsConfig`, Playwright TS-0094 | Object-level IDOR (SR-11). `Escribano` is admin (SR-16) |
| A02 Cryptographic failures | BCrypt cost 12. TLS required in production by policy. JWT signed | `PasswordEncoderUtil`, #254 still open for the certificate guide | TLS material is an operator task |
| A03 Injection | JPA parameters. Input validation strategy | [SQL-INJECTION-PREVENTION.md](SQL-INJECTION-PREVENTION.md), [INPUT-VALIDATION-STRATEGY.md](INPUT-VALIDATION-STRATEGY.md) | None known in Java query construction |
| A04 Insecure design | STRIDE + LINDDUN models, `SR-*` register, Sec by Design program | This folder | Read audit and shared lockout still open |
| A05 Security misconfiguration | Prod compose without public DB or pgAdmin. Swagger denied in production. Security headers | image-pin tests, TS-0044, #1044, #671 | Actuator basic auth must not keep a default password (ADR-019) |
| A06 Vulnerable components | Dependabot, Trivy, image digests/pins, patched floors | `security/tests/`, `PatchedDependencyFloorTest` | Trivy is report-only (SR-13) |
| A07 Identification and authentication failures | Lockout, generic login errors, HttpOnly session, logout revocation | `LoginAttemptServiceTest`, auth guide | Lockout is per JVM (SR-01). No MFA (not claimed) |
| A08 Software and data integrity | cosign, CycloneDX SBOM, secret scan, protected `main` | `cd.yml`, `scan_secrets.py`, ruleset | Audit table is not append-only (SR-09) |
| A09 Logging and alerting failures | Audit aspect for writes. Login metrics. Security dashboard | [MONITORING.md](MONITORING.md) | GET of PII is not audited (SR-08) |
| A10 SSRF | No outbound HTTP client found in the API | Threat-model assumption | Re-check when an integration fetches a user-supplied URL |

Automated checks added with this program: secret scan, policy file, dashboard
and lockout alert guards (`security/tests/test_security_policy.py`). They do
not cover every row. #267 stays open until a reviewer accepts the gaps.
