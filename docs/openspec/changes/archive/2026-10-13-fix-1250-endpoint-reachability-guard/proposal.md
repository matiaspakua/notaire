# Guard: every REST endpoint has a UI consumer or an allowlist reason (#1250)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1250 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `fix/1250_endpoint_reachability_guard` |
| Gate 1 status | draft |

## Objetivo

CONSTITUTION section 4 requires every REST endpoint to be invoked from the UI, but nothing checks it, so endpoints without a consumer pile up unnoticed (assessment #1242 found 10). Add the guard #1250 asks for, with today's unreferenced endpoints as an explicit, reasoned baseline.

## What Changes

- `contracts/tests/test_api_reachability.py` statically scans `frontend/src` (tests and comments excluded) for API literals, method-aware for the `api-client` helpers, and compares them with the OpenAPI endpoints.
- `contracts/api-reachability-allowlist.yaml` lists the 53 endpoints (method + path) the UI does not call today: the 10 from #1250 and 43 more the method-aware scan finds, all marked for triage.
- `contracts/verify.sh` runs the guard (CI and preflight already discover `contracts/tests`); `contracts/MODULE.md` documents it; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A new endpoint without a frontend consumer fails CI unless allowlisted with a reason; stale allowlist entries fail too | #1250, CONSTITUTION §4 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `api-ui-reachability`: Every backend REST endpoint is reachable from the UI or explicitly allowlisted.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | contracts guard, allowlist, MODULE.md |

### Surface area

- Entities / Endpoints / Flyway: none
- Configuration: none

### Architecture review

No architecture change; the guard lives in the contracts module next to the seams guard.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one Added entry |
| `contracts/MODULE.md` | allowlist and guard |
