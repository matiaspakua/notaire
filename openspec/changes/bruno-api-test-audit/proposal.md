# Audit and mature the Bruno API test suite

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #952 |
| Use Case | CU76 |
| Branch | `test/952_bruno-api-test-audit` |
| Gate 1 status | passed |

## Objetivo

The Bruno API test suite (`backend-api/api-test/`) has partial, inconsistent
coverage: several domains have no folder at all, some in-progress folders were
left as bare stubs, and auditing them surfaced a real backend defect
(`Item.fkIdPresupuesto` silently dropped on deserialization) that was masking
incorrect balance/payment behavior. This change brings the suite to a
documented, reconciled state against `TEST-PLAN.md` §7 and fixes the defect
found along the way, so contract coverage reflects the real state of the API
instead of an assumed one.

## What Changes

- Complete the in-progress Bruno folders (`items`, `pagos`, `tramites`,
  `historial`) with full CRUD lifecycle requests and chai `tests {}` blocks.
- Finish and validate the already-renamed `00-auth/` folder and the
  new/modified `auditoria`, `folios`, `inmueble`, `plantilla-presupuesto`,
  `suplencias` folders left as prior WIP on `main`.
- **Fix**: `Item.fkIdPresupuesto` (backend-api `negocio.Item`) was annotated
  `@JsonIgnore`, so `POST/PUT /api/v1/items` never persisted the item's
  budget association; `PagoService.calcularSaldoPendiente()` therefore always
  computed the full budget as pending balance. Switched to
  `@JsonProperty(access = WRITE_ONLY)` so the FK deserializes on write while
  still not leaking the full `Presupuesto` graph on read.
- Regenerate `backend-api/api-test/COVERAGE.md` from the actual endpoint
  inventory.
- Update `TEST-PLAN.md` §7 and `CU-API-MATRIX.csv` Bruno columns to reflect
  actual, current coverage (not 100% — remaining gaps tracked as a follow-up
  Issue, not silently left undocumented).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|--------------------------------|
| An item's budget association (`fkIdPresupuesto`) must be persisted when the item is created or updated, since it determines the budget total used for CU15 payment/balance calculations. | CU76, CU15 | Made explicit (was silently broken in code; no prior Use Case text described the bug, the fix restores intended behavior of CU15) |
| Every REST endpoint must have automated contract coverage with explicit assertions, not a bare call. | CU76 | Made explicit |

## Capabilities

This is a test-infrastructure and defect-fix change; it does not alter any
capability's externally observed requirements — CU15's balance calculation
already required the item/budget association to be honored, this change
restores that existing requirement rather than introducing a new one.

### New Capabilities
_None._

### Modified Capabilities
_None — `skip_specs: true` set in `.openspec.yaml` (test tooling + bugfix
restoring already-specified behavior, no new/changed requirement text)._

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `negocio/Item.java` Jackson annotation fix; no API contract/shape change |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Item` (Jackson visibility annotation only — no schema change)
- Endpoints: `POST/PUT /api/v1/items`, `GET /api/v1/pagos/presupuesto/{id}/saldo`, `POST /api/v1/pagos` (behavior corrected, no contract/shape change); plus new Bruno contract-test coverage for `items`, `pagos`, `tramites`, `historial`, `auditoria`, `folios`, `inmueble`, `plantilla-presupuesto`, `suplencias`, `00-auth`
- Database (Flyway `V{n}`): none — no schema change
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows existing architecture: Bruno YAML/OpenCollection convention already
established in `backend-api/api-test/README.md`; the `Item` fix keeps the
existing Jackson-on-entity pattern used elsewhere (e.g. `Tramite`,
`Presupuesto`) rather than introducing a new serialization mechanism. Not
architectural; no ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `backend-api/api-test/COVERAGE.md` | Regenerated to reflect actual current endpoint coverage |
| `docs/300-development/303-testing/TEST-PLAN.md` | §7 Bruno/API Contract Tests coverage numbers updated |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | Bruno coverage columns updated per endpoint |
| `CHANGELOG.md` | Entry for the `Item` budget-association fix (user-visible: payment balance calculation) |

## Out of Scope

- Authoring Bruno folders for the ~16 controllers with zero existing coverage
  (`CarpetaTramite`, `Copia`, `Cuaderno`, `DocumentoPresentado`, `Gestion`,
  `MinutaInscripcion`, `MovimientoTestimonio`, `PlantillaCostoDocumento`,
  `ProtocoloAuxiliar`, `Reporte`, `Rol`, `Testimonio`, `WorkflowDefinition`,
  `WorkflowNode`, `WorkflowTransition`, `WorkflowValidation`) — tracked as a
  new follow-up GitHub Issue linked to CU76, since it is a substantial,
  independently schedulable body of work rather than part of finishing the
  already-started folders.
