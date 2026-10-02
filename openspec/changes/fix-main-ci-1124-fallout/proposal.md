# Restore main CI after #1124 DTO request fallout

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1067 |
| Use Case | CU39 — Crear Plantilla Presupuesto |
| Branch | `cursor/fix-main-ci-1124-fallout-69d3` |
| Gate 1 status | passed |

## Objetivo

After merging [#1124](https://github.com/matiaspakua/notaire/pull/1124) (#1068
request-body DTO bindings), `main` CI went red: unit tests reflected old
`DeedController.create(Deed)` signatures, integration/Bruno helpers posted
entity-shaped JSON, and `BudgetTemplateController.create` mapped DTO IDs into
the PK only — leaving `concept` / `procedureType` null so
`BudgetTemplateJpaController.create` NPE'd (HTTP 500).

This hotfix restores green CI: align tests with DTO contracts, set concept and
procedureType references before legacy JPA create, and add this OpenSpec folder
so Process Checks pass. Tracked under open QA/test Issue #1067 for Gate 1;
technical fallout is from merged #1124 / #1068 — this PR does **not** invent a
`Closes` for a wrong issue.

## What Changes

- Fix `BudgetTemplateController.create` to `setConcept` /
  `setProcedureType` via repository `getReferenceById` before JPA create.
- Align `AuditAspectTest` reflection with `DeedRequest`.
- Align integration helpers (Management, Substitution, Copy, BudgetTemplate,
  Concept referential) to DTO field names.
- `@NotBlank` on `ManagementRequest.encabezado` so missing encabezado returns
  400 instead of cascading into DB 500.
- Add this OpenSpec change folder for Process Checks.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Creating a budget template requires valid procedure type + concept FKs | CU39 | Made explicit (DTO → entity refs) |
| API create bodies use request DTOs, not JPA entities | #1068 / CU39 | Made explicit in tests |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `budget-template-dto-create`: DTO create hydrates concept/procedureType for legacy JPA.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | BudgetTemplateController create mapping; ManagementRequest `@NotBlank`; tests |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD | no | Process Checks via this change folder |
| `openspec/changes/` | yes | This Gate 1 folder |

### Surface area

- Entities: none (association refs set for create only)
- Endpoints: `POST /api/v1/plantilla-presupuestos` (behavior restored to 201/409)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No ADR. Restores intended #1068 DTO contract against legacy
`BudgetTemplateJpaController` that still requires entity associations.

## Documentation Impact

- No permanent docs update required beyond this change folder (hotfix).
- Changelog optional — CI restore / security DTO fallout.
