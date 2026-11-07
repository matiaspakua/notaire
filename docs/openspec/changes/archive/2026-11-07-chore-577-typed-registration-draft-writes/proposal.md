# Type RegistrationDraftController write returns via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU82 |
| Branch | `refactor/577_typed-registration-draft-writes` |
| Gate 1 status | passed |

## Objetivo

`RegistrationDraftController` generate, submit, observe, and recordRegistration declare `ResponseEntity<Object>` because success bodies, empty 404s, and `{"message"}` 400s share one method. Clients already depend on those HTTP shapes (CU82).

## What Changes

- Domain exceptions plus `RegistrationDraftExceptionHandler` map NotFound and Invalid.
- The four write methods return `ResponseEntity<DtoRegistrationDraft>`.
- Shrink `controller-loose-returns.txt` by those four lines.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Generate, submit, observe, and record registration keep the same 201/200, empty 404, and `{"message"}` 400 bodies | CU82 / Issue #577 | Made explicit |

## Capabilities

### New Capabilities

- None

### Modified Capabilities

- `controller-typed-returns`: the four RegistrationDraft write handlers leave the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, domain exceptions, controller test, baseline |

### Surface area

- Entities: none
- Endpoints: `POST /api/v1/registration-drafts`, `PUT /{id}/submit`, `PUT /{id}/observe`, `PUT /{id}/register` — HTTP contract unchanged
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/registrationdraft/**`
- `backend-api/src/main/java/com/licensis/notaire/domain/registrationdraft/**`
- `backend-api/src/test/java/com/licensis/notaire/unit/RegistrationDraftControllerTest.java`
- `backend-api/src/test/resources/architecture/controller-loose-returns.txt`
- `CHANGELOG.md`
- `docs/openspec/**`

### Architecture review

Follows the #577 advice pattern already used by ProcedureFolder putOnHold and Testimony writes. No new ADR. Controllers still call inbound ports only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased line, Refs #577 |
| `docs/openspec/specs/controller-typed-returns/spec.md` | Archive merges the ADDED requirement |

## Out of Scope

- Closing #577 (multi-PR epic; this tip uses Refs)
- `ProcedureFolderController#putOnHold` (shipped in #2068)
- Other controllers still listed in the loose-return baseline
