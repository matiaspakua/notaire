# Client-side error beacon (safe log endpoint)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1483 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `cursor/feat-1483_client-error-beacon-69d3` |
| Gate 1 status | passed |
| Epic | #1461 (partial slice — do not close the epic) |

## Objetivo

Browser errors today stay in the browser console and never reach Loki. Operators
cannot see client-side failures next to server logs for CU77. This slice adds a
validated, size-capped, rate-limited beacon so structured client errors land in
the frontend server log with `service=notaire-frontend`.

## What Changes

- Add `POST /api/client-errors` on the Next.js frontend (outside `/api/v1/**`).
- Validate allow-listed fields (`level`, `message`, optional `name`/`path`/`stack`),
  enforce body and field size caps, and rate-limit per client key.
- Emit one structured JSON log line via `logger` (Promtail → Loki).
- Unit tests for valid, malformed, oversized, and rate-limited input.
- Optional thin browser helper so `api-client` can POST instead of only logging locally.
- Update monitoring README. CHANGELOG held (serialize / not required for ops path).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Client errors reach Loki as `service=notaire-frontend` | CU77 / #1483 | New |
| Beacon rejects non-allowlisted or oversized payloads | CU77 / #1483 Context | New |
| Beacon rate-limits per client to limit abuse | CU77 / #1483 Context | New |
| Beacon never logs cookies, Authorization, or query PII | CU77 / ADR-016 | Made explicit |

## Capabilities

### New Capabilities

- `client-error-beacon`: safe unauthenticated POST that writes validated client
  error entries to the frontend structured log.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | No | |
| `frontend` | Yes | beacon route, lib, Vitest, optional reporter hook |
| `frontend-swing` | No | |
| `infra` / observability | No | Promtail already scrapes frontend JSON |
| CI/CD (`.github/workflows`) | No | |

### Surface area

- Entities: none
- Endpoints: `POST /api/client-errors` (Next.js only; not Spring OpenAPI)
- Database (Flyway): none
- Configuration / `.env`: none
- Dependencies: none new
- Breaking: no

### Architecture review

Follows the existing frontend logger + Promtail pipeline (ADR-016). No new ADR.
Unauthenticated write is intentional for beacons; controls are allowlist, caps,
and in-memory rate limit (same single-instance spirit as login lockout).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/207-monitoring/README.md` | Document beacon path, fields, LogQL tip |
| `CHANGELOG.md` | Held — ops/observability path; serialize elsewhere |

## Out of Scope

- Full Playwright E2E infra proof (#1469 — Owner-claimed separately).
- Closing epic #1461 (remaining slices must finish first).
- React `error.tsx` / global Error Boundary wiring beyond a minimal reporter helper.
- Multi-replica distributed rate limiting.
- Backend Spring beacon endpoint.
