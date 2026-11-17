# docs: clear stale 500-ERROR rows that already have a green Bruno file

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1244 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/1244-matrix-status-47dc` |
| Gate 1 status | passed |

## Objetivo

Eleven CU-API matrix rows still say `Endpoint_Status=500-ERROR` while the Bruno file named on the same row asserts HTTP 200, 201 or 204. The status column is stale. This change records OK for those rows and guards the matrix so a Bruno-backed row cannot be marked 500-ERROR again.

## What Changes

- Set `Endpoint_Status` to OK on the eleven rows whose `Bruno_Test` is a real file and whose status is `500-ERROR`.
- Replace notes that say the base GET returns 500 with a note that the named Bruno file asserts success.
- Add a unit test that fails if a Bruno-backed row is marked `500-ERROR`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A matrix row that names a Bruno file asserting success is not a 500 error | CU76; #1244 | Made explicit |

## Capabilities

### New Capabilities

- None. This change corrects recorded statuses. It does not add a product capability.

### Modified Capabilities

- None. The matrix guard is a unit test, not a change to an OpenSpec requirement. `.openspec.yaml` sets `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Bruno files stay as they are |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | The matrix test already runs in preflight |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- Not **BREAKING**.

### Write set

- `docs/300-development/303-testing/CU-API-MATRIX.csv`
- `workspace/tests/test_validate_cu_api_matrix.py`
- `docs/openspec/changes/docs-1244-matrix-500-status/**`

### Architecture review

No architectural change. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | Eleven stale 500-ERROR cells become OK |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- `GET /api/v1/deeds/available-notaries` (CU48) stays `500-ERROR` / `NO-TEST`. No Bruno file asserts it.
- New Bruno tests for the other `NO-TEST` rows.
- New Playwright or backend tags for CU72, CU73 and CU79.
- NFR mapping for CU74–CU78.
- #1244 stays open for those remaining acceptance items.
