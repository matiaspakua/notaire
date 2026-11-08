# OWASP Top 10 — Notaire mapping

Engineering map for CU78. Issue **#267** is closed. This table is not a
certification. Verification names a file or a workflow, not a percentage.
The 2026-11-05 review is [ASSESSMENT-2026-11-05.md](ASSESSMENT-2026-11-05.md).

| ID | Risk | Control | Verification | Gap |
|----|------|---------|--------------|-----|
| A01 Broken access control | `ROLE_ADMIN` on admin paths and admin writes. Module grants on API paths. Frontend admin routes redirect | `SecurityAndCorsConfig`, `RoleModuleAuthorizationFilter` (#1683), Playwright TS-0094 | Object-level IDOR (SR-11): cross-notary impossible by single-tenant deployment, intra-office accepted (ADR-029). A notary is not an administrator (`ROLE_NOTARY`, SR-16 mitigated by #1638). A wider regression pack is #2006 |
| A02 Cryptographic failures | BCrypt cost 12. TLS required in production by policy. JWT signed | `PasswordEncoderUtil`, [HTTPS-TLS-RUNBOOK.md](HTTPS-TLS-RUNBOOK.md) (#254 closed) | Production certificates are supplied at deploy time and are not stored in git |
| A03 Injection | JPA parameters. Input validation strategy | [SQL-INJECTION-PREVENTION.md](SQL-INJECTION-PREVENTION.md), [INPUT-VALIDATION-STRATEGY.md](INPUT-VALIDATION-STRATEGY.md) | None known in Java query construction |
| A04 Insecure design | STRIDE + LINDDUN models, `SR-*` register, Sec by Design program | This folder | Read audit and shared lockout still open |
| A05 Security misconfiguration | Prod compose without public DB or pgAdmin. Swagger denied in production. Security headers | image-pin tests, TS-0044, #1044, #671 | Actuator basic auth: `ProductionCredentialsGuard` refuses empty, <16-character, deny-listed or `change-me` passwords in production (#1639, ADR-019) |
| A06 Vulnerable components | Dependabot, Trivy, image digests/pins, patched floors | `security/tests/`, `PatchedDependencyFloorTest`, `ci.yml` | Filesystem scan fails the release job on fixed CRITICAL/HIGH. Image scan fails it on CRITICAL (SR-13). Neither runs on a pull request (#2001) |
| A07 Identification and authentication failures | Lockout, generic login errors, HttpOnly session, logout revocation | `LoginAttemptServiceTest`, auth guide | Lockout state is shared through the `login_attempts` table (SR-01, #1634). No MFA (not claimed) |
| A08 Software and data integrity | cosign, CycloneDX SBOM, secret scan, protected `main` | `cd.yml`, `scan_secrets.py`, ruleset | Audit table is append-only through a trigger and REVOKE (V47, #1636, SR-09); no hash chain against a superuser |
| A09 Logging and alerting failures | Audit aspect for writes and for `Person` reads (`@AuditPersonalDataRead`, #1635). Login metrics. Security dashboard | [MONITORING.md](MONITORING.md) | Reads of entities other than `Person` are not audited until they opt in |
| A10 SSRF | No outbound HTTP client found in the API | Threat-model assumption | Re-check when an integration fetches a user-supplied URL |

Automated checks added with this program: secret scan, policy file, dashboard
and lockout alert guards (`security/tests/test_security_policy.py`). They do
not cover every row. #267 is closed. Open gaps are the issues in
[CYBERSECURITY-PROGRAM.md](CYBERSECURITY-PROGRAM.md) section 9.
