# Type ManagementController#archiving via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU16 |
| Branch | `refactor/577_typed-management-archive` |
| Gate 1 status | passed |

## Objetivo

`ManagementController#archiving` declares `ResponseEntity<Object>` because a `DtoArchivedManagement` success body, an empty 404, and a 409 map (`message` plus `waitingFolderNumbers`) share one method. Clients already depend on those HTTP shapes (CU16, CU85 exception 5.1).

## What Changes

- Domain exceptions plus `ManagementArchiveExceptionHandler` map NotFound and waiting folders.
- `archiving` returns `ResponseEntity<DtoArchivedManagement>`.
- Shrink `controller-loose-returns.txt` by that one line.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Archive keeps 200 `DtoArchivedManagement`, empty 404, and 409 `{message, waitingFolderNumbers}` | CU16 / CU85 exception 5.1 / Issue #577 | Made explicit |

## Capabilities

### New Capabilities

- None

### Modified Capabilities

- `controller-typed-returns`: `ManagementController#archiving` leaves the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, domain exceptions, controller test, baseline |
| `frontend` | no | HTTP contract unchanged |
| `frontend-swing` | no | none |
| `infra` / observability | no | none |
| CI/CD (`.github/workflows`) | no | none |

### Surface area

- Entities: none
- Endpoints: `POST /api/v1/managements/{id}/archive` — HTTP contract unchanged
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/management/ManagementController.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/management/ManagementArchiveExceptionHandler.java`
- `backend-api/src/main/java/com/licensis/notaire/domain/management/ManagementArchive*.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/ManagementArchiveControllerTest.java`
- `backend-api/src/test/resources/architecture/controller-loose-returns.txt`
- `docs/300-development/303-testing/INVENTORY.md`
- `CHANGELOG.md`
- `docs/openspec/**`

### Architecture review

Follows the #577 advice pattern already used by ProcedureFolder putOnHold and RegistrationDraft writes. No new ADR. The controller still calls the inbound port only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased line, Refs #577 |
| `docs/openspec/specs/controller-typed-returns/spec.md` | Archive merges the ADDED requirement |

## Out of Scope

- Closing #577 (multi-PR epic; this tip uses Refs)
- `ManagementController#create`, `#transition`, and the other loose returns
- `UserController#login` and `#logout` (they do not share this advice)
- RegistrationDraft (shipped in #2077)
