# fix: add a per-JVM quota on API calls

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2010 |
| Use Case | CU78 — Seguridad, Privacidad y Cumplimiento |
| Branch | `cursor/2010-api-quota-47dc` |
| Gate 1 status | passed |

## Objetivo

`/api/**` had no general quota. Login lockout was the only denial-of-service control. This slice adds a per-JVM fixed window and records that it is not shared across instances.

## What Changes

- Count API calls per authenticated username, or per remote address when the caller is anonymous.
- Answer 429 with `Retry-After` when the window is full.
- Leave login lockout unchanged.
- Mark SR-15 and SR-05 Partial. #2010 stays open for a shared store.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Default ceiling is 2000 requests per 60 seconds per key | this change | New |
| Login lockout is not replaced | #560, #1634 | Made explicit |
| Counters are not shared across API instances | ADR-018 gap, #2010 | Made explicit |

## Capabilities

### New Capabilities

- `api-quota`: a per-caller fixed window on `/api/**`.

### Modified Capabilities

- None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Filter, quota, security chain |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` | no | |
| CI/CD | no | |

### Surface area

- Endpoints: `/api/**` gains 429 when a key exceeds the window
- Not **BREAKING** for clients that stay under 2000 requests per minute per key.

### Write set

- `ApiRequestQuota.java`
- `ApiRateLimitFilter.java`
- `SecurityAndCorsConfig.java`
- `application.properties`
- `ApiRequestQuotaTest.java`
- threat model and STRIDE register

### Architecture review

Follows the existing security filter chain. The filter runs after JWT authentication so a logged-in caller is not collapsed onto the proxy address.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `THREAT-MODEL-SYSTEM.md` | SR-15 and AT-6 |
| `THREAT-MODEL-AUTH-AUDIT.md` | SR-05 |
| `system.stride.yml` | SR-15 status |

## Out of Scope

- A quota store shared by several API instances. #2010 stays open for that.
- Changing login lockout.
