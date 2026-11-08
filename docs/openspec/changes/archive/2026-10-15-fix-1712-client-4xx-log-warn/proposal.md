# Client 4xx API conflicts log as WARN; prune merged accepted-breaking files

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1712 (also #1713 prune slice of #1315) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-1712-4xx-log-warn-ba90` |
| Gate 1 status | pending |

## Objetivo

A full Bruno run on tip is green, but the backend still emits **ERROR** for expected **4xx** client conflicts (identification-type hexagon 409 via `GlobalExceptionHandler`, management FK delete 409). That noise hides real faults. Separately, four merged `accepted-breaking-changes.d/*.txt` files still sit on main and should be pruned so the OpenAPI ignore list stays empty by default (#1315 / #1713).

## What Changes

- `GlobalExceptionHandler.handleNotaireException` logs **WARN** for status &lt; 500 and **ERROR** only for 5xx (same rule as `ErrorResponses`).
- Ad-hoc delete catch blocks that answer 409 on FK conflict (`ManagementController` and the same pattern touched in this PR) log **WARN**, not ERROR.
- Delete the four stale `backend-api/openapi/accepted-breaking-changes.d/*.txt` files already merged on main (#1713).
- Unit tests prove the log-level split; Bruno remains green; OpenAPI yaml unchanged.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Expected client failures (HTTP 4xx) are logged as WARN; server faults (5xx) as ERROR | CU76, `ErrorResponses` practice | Made explicit for `NotaireException` and conflict deletes |
| Accepted OpenAPI breaks do not outlive their PR; merged `.d` files are pruned | #1315, `openapi-breaking-change-gate` | Applied (delete stale files) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `backend-logging`: client 4xx paths must not emit ERROR structured logs.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `GlobalExceptionHandler`, conflict delete log level, tests; prune openapi `.d` files |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | log severity only |
| CI/CD (`.github/workflows`) | no | existing checker already reports prune |

### Surface area

- Entities: none
- Endpoints: HTTP status codes unchanged; log severity only
- Database (Flyway): none
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING**: no

### Architecture review

Keeps hexagonal adapters coherent (#1693): identification-type domain exceptions still map through `IdentificationTypeExceptionHandler` → `GlobalExceptionHandler`; only logging severity changes. No new Owner product.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased entry for log severity + accepted-breaking prune |
| `docs/openspec/specs/backend-logging/spec.md` | Synced on archive |

## Out of Scope

- Full #1315 OpenAPI coherence (required fields / documenting every 4xx).
- Disabling OTLP when the collector host is missing (env noise, not API contract).
- Rewriting all 26 ad-hoc controller try/catch blocks (#579) — only the delete paths that emitted ERROR during Bruno triage.
- Closing accidental probe issue #1710 (needs human/admin).
