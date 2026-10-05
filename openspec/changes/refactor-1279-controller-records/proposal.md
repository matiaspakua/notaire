# Controllers return records, not JPA entities (slice 1 of #577)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1279 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `refactor/1279_controller_signatures_records` |
| Gate 1 status | draft |

## Objetivo

Eight REST controllers expose JPA entities in 14 public method signatures (Budget 2, Deed 1, DocumentCostTemplate 1, Folio 4, Management 1, AuxiliaryProtocol 1, Notebook 2, Procedure 2). The entities serialize related entities in full (for example the notary `Person` with its tax id) and have caused Hibernate-proxy and cyclic-serialization bugs. Return per-endpoint records instead, guard the rule with a test, and delete the two DTO classes nothing uses. This is slice 1 of #577; later slices remove the entity `getDto()`/`setAtributo()` mapping, move repository-calling controllers behind services and delete the `dto` package.

## What Changes

- An architecture test fails when a public handler of a `@RestController` mentions a `business` entity type; a second test holds loosely typed returns (`ResponseEntity<Object>`, `<?>`) to a baseline that can only shrink.
- The 14 handlers return nested response records next to their controller, with related entities as slim references (`personId` and `notaryRegistrationNumber` for a notary, ids and names for types), built by a `from` factory on the record.
- `PersonResponse`, `ItemResponse` and `ManagementResponse` become public and gain `from` factories so other controllers reuse them.
- `DtoFlag` and `DtoIdentification` are deleted.
- `backend-api/openapi/openapi.yaml` is regenerated.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Entities never leave the service layer; controllers return records | `.claude/rules/refactoring.md`, #577 | Made explicit |
| A related entity is exposed as a slim reference, never in full | #577 (PII, lazy loading) | New |

## Capabilities

### New Capabilities

- `controller-response-records`: REST handlers answer with records and the rule is enforced by a test.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | 8 controllers, 3 shared records, 2 deleted DTOs, new tests |
| `frontend` | no | its types already declare the slim shapes (`fkIdNotaryPerson` has `personId` and `notaryRegistrationNumber`) |
| Docs / scripts / CI | yes | OpenAPI, `.claude/rules/refactoring.md`, CHANGELOG |

### Surface area

- Endpoints: 14 handlers keep their paths and status codes; their JSON loses nested entity detail and entity internals (BREAKING only for a client reading nested fields the frontend types do not declare)
- Entities / Flyway / Configuration: none
- Dependencies: none (reflection and Spring classpath scanning, no ArchUnit)
- Risk: a client reading a dropped nested field; mitigated by checking the frontend types, Bruno and Playwright, and by listing the dropped fields in the CHANGELOG

### Architecture review

Follows `.claude/rules/refactoring.md` (never expose JPA entities from controllers) and the existing record convention of `PersonController` and `DeedController`. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `backend-api/openapi/openapi.yaml` | regenerated; schemas of the 14 handlers narrow |
| `.claude/rules/refactoring.md` | state the enforced guard |
| `CHANGELOG.md` | one entry |
