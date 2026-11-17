# fix: prefer the most specific API template in the reachability scan

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1563 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/1563-reachability-specificity-47dc` |
| Gate 1 status | passed |

## Objetivo

A frontend call such as `apiGet('/payments/date?...')` currently counts as a call to both `GET /payments/date` and `GET /payments/{id}`. The reachability gate then hides read-by-id endpoints that the UI never calls. This change keeps only the template with the fewest wildcards and records the newly visible endpoints on the allowlist.

## What Changes

- `reachable()` keeps, among exact fits, only the templates with the fewest `{param}` segments.
- A unit test proves `/payments/date` does not cover `GET /payments/{id}`.
- Endpoints that the stricter scan shows as uncalled are allowlisted as bucket A (REST read-by-id; the UI works from lists), the decision already used for the same kind of endpoint in #1250.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A UI literal covers the most specific matching OpenAPI template | CU76; CONSTITUTION §4; #1563 | Made explicit |
| A read-by-id endpoint the UI does not call stays API-only with a written reason | CU76; #1250 bucket A | Made explicit |

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `api-ui-reachability`: a fixed-segment call no longer counts as a call to a parameterized sibling.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | The same guard already runs in preflight |

### Surface area

- Entities: none
- Endpoints: none added or removed. The scan stops treating some existing GET-by-id routes as called.
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- Not **BREAKING** for API clients.

### Write set

- `contracts/tests/test_api_reachability.py`
- `contracts/api-reachability-allowlist.yaml`
- `docs/openspec/changes/fix-1563-api-reachability-specificity/**`

### Architecture review

No architectural change. No ADR. The scanner stays a static heuristic.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `contracts/api-reachability-allowlist.yaml` | New bucket A reasons for the endpoints the stricter scan uncovers |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- Wiring those read-by-id endpoints into the UI.
- Removing endpoints from OpenAPI.
- #1250 bucket D removals.
