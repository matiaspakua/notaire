# security: share the API quota across instances

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2010 |
| Use Case | CU78 — Seguridad, Privacidad y Cumplimiento |
| Branch | `cursor/2010-shared-quota-47dc` |
| Gate 1 status | passed |

## Objetivo

The `/api/**` quota counted in one JVM. A second API instance did not see that count. Login lockout was already a shared table.

## What Changes

- `api_rate_windows` stores the bucket key, the window start, and the count.
- Every API instance uses that table. The in-memory window remains the unit-test double.
- Login lockout is unchanged. Lockout-griefing of a known username stays SR-05.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A caller who exceeds the window receives 429 and Retry-After | CU78, #2010 | Made explicit |
| The count is shared by every API instance | CU78, #2010 | Changed |
| Login lockout stays a separate control | CU78 | Made explicit |

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `api-quota`: the counter is the shared table, not the API process.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | V55 and the quota store |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Entities: `api_rate_windows` (bucket key, window start, count). No request body.
- Endpoints: `GET/POST /api/**` still return 429 when the shared window is spent.
- Flyway: V55.
- Not **BREAKING** for a single instance. A second instance now shares the count.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` | SR-15 and AT-6 |
| `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md` | SR-05 |
| `docs/200-architecture/206-security/threat-model/system.stride.yml` | SR-15 |
| `docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md` | SR-15 and the gap row |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | open-work table |
| `docs/200-architecture/202-ADR/ADR-018-rate-limiting-policy.md` | status note |
| `docs/openspec/specs/api-quota/spec.md` | shared counter |

## Out of Scope

- Redis or a gateway limiter.
- Stopping lockout-griefing of a known username.
