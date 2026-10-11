# Security policy as code and Sec by Design program

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #307 |
| Use Case | CU78 — Seguridad, Privacidad y Cumplimiento |
| Branch | `cursor/security-policy-307-81d6` |
| Gate 1 status | passed |

## Objetivo

Give Notaire a Sec by Design program that matches the specialization guide
(threat model, security requirements, architecture, toolchain, monitoring) and
enforce the first policy-as-code check: a failing secret scan.

## What Changes

- Permanent security docs: program, system threat model (STRIDE + LINDDUN),
  OWASP Top 10 map, toolchain, monitoring, trust-boundary diagram.
- `security/policy/security-policy.yml` and `security/scan_secrets.py`.
- CI security job fails when the scanner finds a high-confidence secret.
- Grafana dashboard `notaire-security` and Prometheus alert `LoginLockoutSpike`.
- Correct the stale claim that role checks do not exist. Function-level RBAC
  is already in `SecurityAndCorsConfig`.
- AI SDLC fleet note: security-sensitive diffs update the threat model and run
  `bash security/verify.sh`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Personal and notarial data stay confidential, intact, and available | CU78, RF #81–#85 | Made explicit as SR-* with verification |
| A secret in git fails the pipeline | CU78, #307 | New |
| Admin paths require an administrator authority | CU78, #559 | Made explicit; SAD text corrected |
| Exceptions to a security control are time-boxed and owner-approved | #307 | New |

## Capabilities

### New Capabilities

- `security-policy-as-code`: executable security policy, secret scan, and the Sec by Design knowledge base the policy points at.

### Modified Capabilities

- None. Existing product behavior of login and RBAC does not change.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `security` | yes | Policy, scanner, tests, verify script |
| `docs` | yes | Program, threat model, SAD correction, CU78 pointer, fleet note |
| `infra` / observability | yes | Security dashboard and lockout alert |
| CI/CD (`.github/workflows`) | yes | Secret scan step in `ci.yml` |
| `backend-api` | no | No product code |
| `frontend` | no | Pages site copy only under `github-page` |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: CI installs `pyyaml==6.0.1` only for the scan step. The application classpath is unchanged.

### Architecture review

Follows the existing security module (ADR-024: scans stay next to the source).
No new ADR. Tool selection is recorded in `SECURITY-TOOLS.md`. The branch name
uses the cloud-agent template instead of `<type>/<issue>_<desc>`; see
traceability exceptions.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | New program |
| `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` | New system model |
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | New toolchain |
| `docs/200-architecture/206-security/MONITORING.md` | New monitoring note |
| `docs/200-architecture/206-security/OWASP-TOP10.md` | New mapping, #267 stays open |
| `docs/200-architecture/206-security/README.md` | Index and corrected access-control row |
| `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md` | SR-06 Partial |
| `docs/200-architecture/201-SAD/sad.md` | Authorization text matches the code |
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Pointer to the program |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | Security gate |
| `CHANGELOG.md` | Unreleased security entry |

## Out of Scope

- Implementing API rate limiting (#280), a secrets vault (#283), production TLS material (#254), or a failing ZAP gate (#281).
- Closing #267 or #309. Those need a human review and a legal mapping.
- Exploit procedures, sqlmap, or any active test against a shared environment.
- Changing who counts as `ROLE_ADMIN` (`Escribano` stays admin until a product decision, SR-16).
