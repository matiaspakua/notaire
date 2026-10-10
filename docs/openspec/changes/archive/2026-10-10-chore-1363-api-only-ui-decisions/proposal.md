# Record the Owner's API-only decisions in the reachability allowlist (#1363, #1364)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1363, #1364 |
| Use Case | CU42 – Documentos por vencer; CU44 – Reingreso testimonio; CU76 – Validación (CONSTITUTION section 4) |
| Branch | `chore/1363_api_only_ui_decisions` |
| Gate 1 status | draft |

## Objetivo

On 2026-10-09 the Owner decided (UI/UX epic #1336) that the expiring-documents report PDF and manual testimony-movement editing stay API-only. The reachability allowlist still listed the report as bucket E (waiting for a decision) and the movement POST/PUT/DELETE as bucket C (UI backlog). Record the decision so #1250 and the allowlist agree.

## What Changes

- `contracts/api-reachability-allowlist.yaml`: `GET /reportes/documentos-por-vencer/{id}` and `POST/PUT/DELETE /movimiento-testimonio` become API-only (bucket A) with the decision and issue; the header drops bucket E.
- `GET /movimiento-testimonio` stays UI backlog: #1364 keeps the testimony movement history panel.
- `contracts/tests/test_api_reachability.py`: no entry may still say `Owner to reclassify`; the four entries must say API-only.
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The expiring-documents report has no UI action | Owner decision 2026-10-09 (#1363) | New |
| Testimony movements are created only by withdraw/re-enter; manual edit is API-only | Owner decision 2026-10-09 (#1364) | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `api-reachability`: Every OpenAPI endpoint is called from the UI or allowlisted with a decided reason.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `contracts` | yes | allowlist and its test |
| `frontend` | no | — |
| `backend-api` | no | — |

### Surface area

- Endpoints: none change; only their reachability classification
- Entities / Flyway / configuration: none

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Changed entry |
| `contracts/api-reachability-allowlist.yaml` | reasons and header |
