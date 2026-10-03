# Refresh CU-API-MATRIX after English rename + CI validator

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1064 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/docs-1064-cu-api-matrix-refresh-69d3` |
| Gate 1 status | passed |

## Objetivo

`docs/300-development/303-testing/CU-API-MATRIX.csv` still lists 22 Spanish
REST controller class names removed by the English rename, omits eight live
resources (`/carpetas`, `/cuadernos`, `/minutas-inscripcion`,
`/plantilla-costos-documento`, `/protocolo-auxiliar`, `/roles`,
`/tipo-identificacion`, `/tramites`), and has no CI guard against further drift.
This change refreshes the matrix and adds a preflight/CI validator so the
traceability ledger stays aligned with `adapter.in.web` controllers.

## What Changes

- Rename every stale Spanish `Controller` value in `CU-API-MATRIX.csv` to the
  current English class under `backend-api/.../adapter/in/web/`.
- Add matrix coverage for the eight missing resources (including updating
  CU80–CU82 from `NOT-IMPLEMENTED` and adding CU85 for carpetas).
- Normalize `Bruno_Test` to request paths / folder roots / `MISSING` / `N/A`
  only; leave Bruno request authoring to #953 (do not invent new Bruno fills).
- Ensure every `Bruno_Test=MISSING` row cites `#953` in `Notas` or
  `GitHub_Issue`.
- Add `scripts/validate-cu-api-matrix.py` (+ unittest) and wire it into
  `scripts/preflight.sh` and CI process checks.
- Update `TEST-PLAN.md` / testing README / `CHANGELOG.md` for the validator
  and refreshed matrix (not Bruno fills).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| CU ↔ API matrix MUST name current REST controller classes | CU76; #1064 AC; #605 | Changed (English names) |
| Every live `adapter.in.web` resource base path MUST appear in the matrix | CU76; #1064 AC | Made explicit |
| `Bruno_Test` MUST hold paths or sentinels `MISSING`/`N/A` only | CU76; #1064 AC | Made explicit |
| Matrix rows with `Bruno_Test=MISSING` MUST link Bruno gap issue #953 | CU76; #953 | Made explicit |
| CI/preflight MUST reject matrix drift vs live controllers | CU76; #1064 AC | New |

## Capabilities

### New Capabilities

- `cu-api-matrix-validation`: CI-facing rules that keep
  `CU-API-MATRIX.csv` aligned with English REST controllers, required resource
  bases, and Bruno_Test column conventions.

### Modified Capabilities

None under `openspec/specs/` (documentation + tooling; product API unchanged).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no (product) | Reads controller sources for validation only; no runtime change |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | Process/self-test job runs the new validator via `scripts/tests` / preflight mirror |
| `docs/300-development/303-testing/` | yes | Matrix refresh + TEST-PLAN/README notes |
| `scripts/` | yes | `validate-cu-api-matrix.py` + unittest + preflight wiring |

### Surface area

- Entities: none
- Endpoints: none (documentation of existing endpoints only)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (stdlib Python)

### Architecture review

Follows existing process-script pattern (`validate-sdlc-plan.sh` +
`scripts/tests/*.py`). No ADR. Does not use `local-ai/`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | English Controllers; eight resources; Bruno_Test normalization; #953 on MISSING |
| `docs/300-development/303-testing/TEST-PLAN.md` | Document validator + matrix column conventions |
| `docs/300-development/303-testing/README.md` | Point at validator command |
| `CHANGELOG.md` | Unreleased entry for matrix refresh + CI validator |
| `scripts/preflight.sh` | Blocking check for matrix validation |

## Out of Scope

- Authoring or expanding Bruno requests for uncovered endpoints (#953).
- Renaming Spanish REST URL paths still exposed by the API.
- Product behavior changes in controllers or frontend.
- Closing related location issue #605 (already addressed historically).
