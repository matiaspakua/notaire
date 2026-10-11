# Security Policy

The cybersecurity program (Sec by Design, SAMM snapshot, toolchain, monitoring)
is [CYBERSECURITY-PROGRAM.md](CYBERSECURITY-PROGRAM.md). The system threat model
is [THREAT-MODEL-SYSTEM.md](THREAT-MODEL-SYSTEM.md).

## Supported Versions

We actively support and provide security updates for the following versions:

| Version | Supported          | Notes |
| ------- | ------------------ | ----- |
| 1.x     | :white_check_mark: | Current stable release |

## Reporting a Vulnerability

If you discover a security vulnerability within Notaire, please send an e-mail to the maintainer. All security vulnerabilities will be promptly addressed.

Please include the following information:

- Type of vulnerability
- Full paths of source file(s) related to the vulnerability
- Location of the affected source code (tag/branch/commit or direct URL)
- Any special configuration required to reproduce the issue
- Step-by-step instructions to reproduce the issue
- Proof-of-concept or exploit code (if possible)
- Impact of the issue, including how an attacker might exploit it

## Data Classification & Threat Model

Notaire manages **notarial deeds and personal data** (`Person` records
include DNI, email, and legal-act involvement) subject to Argentine legal
retention requirements (see the SAD's Legal Constraints, §2.3) — a breach
exposes both PII and legally-binding document metadata, not just
application data.

### Data sensitivity
| Category | Examples | Sensitivity |
|----------|----------|--------------|
| PII | `Person` (DNI, email, firstName/lastName) | High — regulated personal data |
| Legal/notarial records | `Deed`, `Testimony`, `Folio`, `SubmittedDocument` | High — legally binding, indefinite retention |
| Financial | `Budget`, `Payment` | Medium — budget/payment amounts, no card data stored |
| Operational | `AuditRecord`, `History` | Medium — who-did-what audit trail, itself a control |
| Credentials | `User.password` (BCrypt hash) | Critical |

### Primary threats and current mitigations
| Threat | Mitigation | Gap |
|--------|-----------|-----|
| Credential stuffing / brute force on login | `LoginAttemptService` lockout (5 attempts / 15 min) | Single-instance only — see [ADR-018](../202-ADR/ADR-018-rate-limiting-policy.md) |
| Unauthorized data access | JWT on `/api/**` plus `ROLE_ADMIN` for admin paths and admin writes (#559) | Object-level access (one user, another client's record) is still open — see [THREAT-MODEL-SYSTEM.md](THREAT-MODEL-SYSTEM.md) SR-11 |
| Credential leakage via defaults | `ProductionCredentialsGuard` blocks startup on default `.env` values in production | Only catches the literal `"admin"` default, not weak passwords generally — see [ADR-019](../202-ADR/ADR-019-secrets-management.md) |
| API surface reconnaissance | Swagger UI/spec denied in production | Non-production environments remain fully open — see [ADR-020](../202-ADR/ADR-020-openapi-exposure-policy.md) |
| SQL injection | JPA/Hibernate parameterized queries throughout (see [SQL Injection Prevention](SQL-INJECTION-PREVENTION.md)) | None known |
| Tampering with audit trail | `AuditRecordService` records acting user from `SecurityContextHolder` (JWT identity), never a client-supplied header | GETs are not audited (read access to PII is not logged) |

For the two highest-risk subsystems — authentication and the audit trail —
this table is superseded by a formal STRIDE threat model:
[THREAT-MODEL-AUTH-AUDIT.md](THREAT-MODEL-AUTH-AUDIT.md), with numbered
security requirements (`SR-01`..`SR-10`) and an explicit
Mitigated/Partial/Open status per threat. The rest of the system, including
personal data and notarial records, is covered by
[THREAT-MODEL-SYSTEM.md](THREAT-MODEL-SYSTEM.md) (`SR-11` onward).

## Security Features

### Implemented Security Measures

1. **Docker Security**
   - Non-root user execution
   - Minimal base image (Alpine Linux)
   - Read-only file system recommended
   - Security scanning with Trivy

2. **Application Security**
   - Input validation with Spring Boot Validation
   - SQL injection prevention via JPA/Hibernate
   - Secure password storage (bcrypt)
   - CORS configuration

3. **CI/CD Security**
   - Dependency vulnerability scanning (Trivy)
   - SAST scanning (SpotBugs)
   - Docker image scanning
   - SBOM generation

4. **GitHub Security Features**
   - Dependency Graph
   - Dependabot alerts
   - Secret scanning
   - Code scanning alerts

## Dependencies Security

This project uses the following security tools:

- **Trivy**: Vulnerability scanner for containers and dependencies
- **SpotBugs**: Static analysis for Java bytecode
- **Dependabot**: Automated dependency updates
- **GitHub Code Scanning**: SAST integration

## Security Best Practices

When deploying Notaire:

1. **Environment Variables**
   - Never commit secrets to version control
   - Use Docker secrets or environment variables
   - Rotate database credentials regularly

2. **Database**
   - Use strong passwords
   - Enable SSL connections
   - Restrict network access

3. **API Security**
   - JWT authentication on every `/api/**` endpoint (see [API Authentication Guide](API-AUTHENTICATION-GUIDE.md))
   - Login brute-force lockout per username (`LoginAttemptService`, in-memory, single-instance)
   - Enable HTTPS in production
   - General API rate limiting (not yet implemented — only login attempts are throttled)

4. **Docker**
   - Run containers as non-root
   - Use read-only volumes where possible
   - Scan images before deployment
