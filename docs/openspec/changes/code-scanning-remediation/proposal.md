# Code-scanning remediation and shift-left security gates

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1618 |
| Use Case | CU78 |
| Branch | `fix/1618_code-scanning-remediation` |
| Gate 1 status | passed — `openspec validate --strict` and `validate-sdlc-plan.sh` |

## Objetivo

GitHub Code Scanning found seven open alerts that the local security gate did
not detect. Three generated fixes are unsafe or non-compiling, and the browser
uses an ambient HttpOnly JWT cookie while the documentation incorrectly claims
that the API is cookie-less and exempt from CSRF protection.

## What Changes

- Add a double-submit CSRF defense for unsafe browser requests through the
  existing same-origin Next BFF, while retaining Bearer-token API clients.
- Add deterministic local SAST/security-policy coverage for disabled CSRF,
  incomplete dynamic-regex escaping, and missing explicit CD job permissions.
- Apply least-privilege permissions to the three affected CD jobs and complete
  the regex escaping fix.
- Repair the stale repository test that makes `main` fail its process-suite
  self-test.
- Update the security architecture and SDLC evidence to state the real
  cookie-based trust boundary and the new pre-PR detection controls.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A browser-authenticated unsafe API request must prove same-origin intent with a valid CSRF token; SameSite is defense in depth, not the sole control. | CU78; `docs/200-architecture/206-security/INPUT-VALIDATION-STRATEGY.md` | Changed |
| Non-browser API clients using an Authorization Bearer token remain supported without a browser cookie flow. | CU78; `docs/openspec/specs/httponly-jwt-cookie/spec.md` | Made explicit |
| Every CD job declares the minimum GitHub token permissions required for its work. | CU78; `docs/200-architecture/208-devsecops/README.md` | Made explicit |
| Security gates must detect the alert classes before a pull request is opened. | CU78; `docs/openspec/specs/local-security-gates/spec.md` | Changed |

## Capabilities

### New Capabilities
- `code-scanning-remediation`: cross-cutting security remediation and
  pre-PR detection requirements for the three CodeQL alert families.

### Modified Capabilities
- `httponly-jwt-cookie`: browser cookie authentication gains CSRF-token
  requirements for unsafe requests.
- `local-security-gates`: the local SAST/policy gate gains deterministic
  coverage for the alert classes that reached GitHub Code Scanning.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | CSRF filter configuration and integration coverage. |
| `frontend` | yes | CSRF token propagation through the API client/BFF. |
| `frontend-swing` | no | It continues to use Bearer-token API access. |
| `infra` / observability | no | No runtime infrastructure change. |
| CI/CD (`.github/workflows`) | yes | Least-privilege CD job permissions and actionable SAST evidence. |

### Surface area

- Entities: none.
- Endpoints: existing `/api/v1/**`; no new application endpoint.
- Database (Flyway `V{n}`): no migration.
- Configuration / `.env`: no new key; `X-XSRF-TOKEN` becomes an explicit CORS
  allowed header.
- Dependencies: none.
- Compatibility: browser writes without a CSRF token are intentionally rejected;
  existing browser calls receive and return the token through the BFF. Bearer
  clients remain supported.

### Architecture review

The change preserves the Spring Security → Next BFF → browser architecture and
uses Spring Security's cookie-token repository rather than a parallel session
mechanism. It corrects a documented trust-boundary error but does not introduce
a new architectural component; an ADR is not required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Add CSRF protection to the browser-cookie security controls and acceptance criteria. |
| `docs/200-architecture/206-security/INPUT-VALIDATION-STRATEGY.md` | Replace the obsolete cookie-less CSRF posture with the implemented defense and verification. |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | Describe the CSRF header required for unsafe browser requests. |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | Record the shift-left SAST/policy controls and CodeQL alert handling. |
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | Record the added deterministic local controls. |
| `CHANGELOG.md` | n/a — security hardening with no user-facing feature. |

## Out of Scope

- It does not add a stateful server session, alter JWT issuance/expiry, or change
  RBAC; CU78 and `httponly-jwt-cookie` remain the governing records.
- It does not make all historical CodeQL findings globally blocking. A separate,
  owner-approved policy change would be required for severity thresholds and
  backlog triage.
- It does not change ZAP or Trivy's warn-first policy; those are tracked by
  issues #281 and #282 in `CYBERSECURITY-PROGRAM.md`.
