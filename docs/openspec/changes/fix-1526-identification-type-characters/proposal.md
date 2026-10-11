# Identification type `characters` is required and a whole number from 1 to 50; 400 instead of 500 (#1526)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1526 |
| Use Case | CU17 – Registrar persona; RF-53 – Administrar tablas base |
| Branch | `fix/1526_identification_type_characters` |
| Gate 1 status | draft |

## Objetivo

`IdentificationTypeRequest.characters` (the number length the person form uses) wasn't validated. A missing value reached the NOT NULL column and answered 500, and any text ("abc") was stored with 201. The admin form from #1361 accepted 1 to 99.

## What Changes

- Backend `IdentificationTypeController.IdentificationTypeRequest`: `characters` is `@NotBlank` and `@Pattern(^([1-9]|[1-4][0-9]|50)$)` with an `@Schema` description; POST and PUT answer the usual 400 validation body. It stays a string on the wire.
- OpenAPI regenerated (`characters` required, minLength 1, pattern) and `api.generated.ts`; the break is listed in `accepted-breaking-changes.d/1526-identification-type-characters.txt`.
- Frontend admin form: the same 1-to-50 rule and message (was 1 to 99).
- Bruno identification-types 12 (missing → 400) and 13 ("abc" → 400); COVERAGE row.
- Tests: JUnit `IdentificationTypeCharactersValidationIntegrationTest` (14 cases), Vitest case in `identification-types-admin.test.tsx`. CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| `characters` is a whole number from 1 to 50 | identification_types.characters NOT NULL; Owner on #1526 | New (was unvalidated) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `identification-type-validation`: The identification-type API validates the number length.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | request validation, OpenAPI, Bruno |
| `frontend` | yes | form rule, generated types, i18n |

### Surface area

- Endpoints: POST /api/v1/identification-types and PUT /api/v1/identification-types/{id} reject a missing, blank or invalid `characters` with 400 (accepted break)
- Entities / Flyway / configuration: none

### Architecture review

Validation stays at the web adapter (Bean Validation on the request record), as for `name`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/api-test/COVERAGE.md` | identification-types row |
| `backend-api/openapi/accepted-breaking-changes.d/1526-identification-type-characters.txt` | accepted break with the reason |
