# Threat model — Notaire system

Scope: the browser, the Next.js BFF, `backend-api`, and PostgreSQL. Personal
data is in scope, so the model uses STRIDE and LINDDUN, as in the
specialization guide §3.4.1
(`final_projects_specialization.md`). Authentication and the audit trail keep
their own register: [THREAT-MODEL-AUTH-AUDIT.md](THREAT-MODEL-AUTH-AUDIT.md)
(`SR-01`..`SR-10`). This file continues at `SR-11`.

Diagram: [diagrams/trust-boundaries.puml](diagrams/trust-boundaries.puml).

Owners: the Security Officer for acceptance, the Development Team for the
control. Likelihood and impact are ordinal (low, medium, high). They are not
percentages.

## Assets and actors

| Asset | Classification | Examples |
|-------|----------------|----------|
| Credentials | Critical | BCrypt hash, JWT secret, actuator password |
| Personal data | High | `Person` DNI, email, name |
| Notarial records | High | `Deed`, `Testimony`, `Folio` — indefinite retention (SAD §2.3) |
| Financial records | Medium | `Budget`, `Payment` — amounts, no card data |
| Audit trail | Medium | `audit_records`, management history |

Actors: notary staff in the browser, an API client with a Bearer token, an
anonymous caller, a compromised low-privilege user, and an operator with
database access. The browser and any request field are outside the trust
boundary. `X-Notaire-User` is not an identity.

## Attack classes

Names follow the guide §2.5.13. The verification column points at a control
already in the tree. No exploitation procedure is specified here.

| ID | Class | Property | Control in tree | Status |
|----|-------|----------|-----------------|--------|
| AT-1 | Credential abuse | Spoofing | BCrypt, lockout, JWT validation | Partial (SR-02; SR-01 mitigated by #1634) |
| AT-2 | IDOR / object access | Elevation | Authenticated route only, except admin paths; tenants are isolated by deployment (ADR-029) | Mitigated (cross-tenant); accepted (intra-tenant, SR-11) |
| AT-3 | Injection | Tampering | JPA parameters | Mitigated — [SQL-INJECTION-PREVENTION.md](SQL-INJECTION-PREVENTION.md) |
| AT-4 | XSS | Tampering / disclosure | Production CSP nonce, no `unsafe-eval` (#1051) | Partial — encoding still required on every render |
| AT-5 | Cookie and session theft | Spoofing | HttpOnly, SameSite, Secure in production; logout revocation | Partial (SR-12) |
| AT-6 | Denial of service | Availability | Login lockout only | Open (SR-05, SR-15, #280) |
| AT-7 | Dependency or image compromise | Tampering | Dependabot, Trivy, image pins, cosign | Partial (SR-13) |
| AT-8 | Secret in git | Disclosure | This change's scanner; GitHub secret scanning is an admin setting | Partial (SR-14) |
| AT-9 | Role conflation | Elevation | `Escribano` receives `ROLE_NOTARY`, not `ROLE_ADMIN` | Mitigated (SR-16, #1638) |

## STRIDE

| ID | Asset | Boundary | Threat | Impact | Likelihood | Mitigation | Verification | Residual |
|----|-------|----------|--------|--------|------------|------------|--------------|----------|
| SR-11 | Person, Deed | API | Authenticated user reads or changes another client's record by changing an id (IDOR) | High | Medium | Single tenant per notary (ADR-029): no two notaries share a database or signing secret, so cross-notary access cannot happen. Inside a tenant the office shares its files; access is role-based | Isolation checklist (ADR-029 W3); module RBAC enforcement pending (W1, W2) | Cross-tenant: Mitigated by architecture. Intra-tenant: Accepted by design |
| SR-12 | Session | Browser → BFF | Stolen JWT from script-readable storage, or a long-lived Bearer token | High | Low | HttpOnly cookie for the browser (#1051). API clients still receive a token in the login JSON | `frontend` security-header tests; auth guide | Partial |
| SR-13 | Build | CI → registry | Vulnerable dependency or base image is published | High | Medium | Trivy report, Dependabot, pinned images, cosign SBOM | `security/tests/test_image_pins_and_dependabot.py` | Partial — Trivy does not fail CI |
| SR-14 | Repository | Developer → git | Private key or token committed | High | Low | `python3 security/scan_secrets.py` fails the security job | `security/tests/test_security_policy.py` | Partial — push protection is outside git |
| SR-15 | API availability | Internet → API | Expensive endpoints are called without a quota | Medium | Medium | No general limiter. Login lockout is SR-05 | Issue #280 | Open |
| SR-16 | Admin capability | API | A user whose type is Escribano was granted `ROLE_ADMIN`, including user, role, and audit-log administration | High | Medium | `UserAuthorityResolver` maps Escribano to `ROLE_NOTARY`; Flyway V49 promotes the seeded `admin`; other former administrators stored as Escribano must be promoted by `admin` | `UserAuthorityResolverTest`, `RbacIntegrationTest` (notary 403), `80_roles.sql` | Mitigated (#1638) |

`SR-06` in the auth model is **Partial**, not open: `hasRole(ADMIN)` is
enforced for the paths listed in the
[API authentication guide](API-AUTHENTICATION-GUIDE.md). `SR-11` records the
object-level question that `SR-06` used to describe too broadly; ADR-029 closes it by tenancy model.

## LINDDUN

| ID | Privacy threat | Data | Mitigation | Verification | Residual |
|----|----------------|------|------------|--------------|----------|
| LD-1 | Identifiability | DNI, email, name | Access requires a JWT. Field-level minimization is not implemented | Schema and API responses still return identifiers to any authenticated caller | Open |
| LD-2 | Linkability | Same person across management, deed, and budget | Identifiers are stable keys. That is required for the notarial record | SAD data model | Accepted for the notarial purpose |
| LD-3 | Disclosure | Request logs | `RequestLoggingFilter` logs method, path, status, and duration. It does not log query, headers, or body (#1463) | `RequestLoggingFilterTest` | Partial — the path can still contain a record id |
| LD-4 | Unawareness | Data subjects | No in-product privacy notice | Issue #309 | Open |
| LD-5 | Non-compliance | Retention and statute mapping | Notarial retention is indefinite by SAD §2.3. A statute map is not an engineering claim | Issue #309 | Open |
| LD-6 | Detectability of abuse | Login failures | `notaire_operation_total` and Grafana | [MONITORING.md](MONITORING.md) | Partial — read access is not a metric |

Non-repudiation of writes stays `SR-07` (mitigated); `SR-08` (`Person` reads) is
mitigated by #1635 and `SR-09` by #1636 in the auth and audit model.

## Assumptions

- No user-controlled outbound HTTP client was found in `backend-api` (no
  `RestTemplate` or `WebClient`). SSRF is not a current exposure. Re-open the
  model if an outbound integration is added.
- Card data is not stored.
- A pentest has not been executed under this change. Status labels are design
  status, not a claim that an attacker failed.
