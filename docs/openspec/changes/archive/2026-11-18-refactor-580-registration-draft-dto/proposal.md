# refactor: move registration-draft transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-registration-draft-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`business.RegistrationDraft` built `DtoRegistrationDraft`. The ops adapter called `getDto()` on find, generate, submit, observe, and record. This slice moves that mapping off the entity.

## What Changes

- Add `RegistrationDraftLegacyMapper`.
- `RegistrationDraftOpsAdapter` calls the mapper.
- Delete `getDto` from `business.RegistrationDraft`.
- Shrink the entity-DTO ratchet by `RegistrationDraft#getDto`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A draft DTO carries id, number, price, status, the four dates, registry numbers, notes, and the deed id | existing `getDto` | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `.openspec.yaml` sets `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper and the ops adapter |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Entities: `business.RegistrationDraft` no longer maps itself
- Endpoints: none
- Database: none
- Not **BREAKING** for API clients.

### Write set

- `RegistrationDraftLegacyMapper.java`
- `RegistrationDraftOpsAdapter.java`
- `business/RegistrationDraft.java`
- `RegistrationDraftLegacyMapperTest.java`
- `entity-dto-mapping.txt`

### Architecture review

Follows ADR-030. No new ADR. Located with repolens search before the first edit.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| None beyond the code | The HTTP API is unchanged |
| `CHANGELOG.md` | n/a |

## Challenged in the same pass

Substitutions (`GET /api/v1/substitutions`) is a growing list, and the screen filters the whole list by year and notary in the browser. Paging that endpoint without moving those filters to the server would hide rows. That slice waits.

## Out of Scope

- `getDto` on Deed, Folio, Procedure, and the remaining entities.
- Paging substitutions.
