# refactor: move DeedManagement transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2329 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2329-deed-management-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`DeedManagement#getDto` is the last line on the entity-mapping ratchet. The HTTP API does not change.

## What Changes

- The method body moves to `DeedManagementLegacyMapper.toDto`.
- `getDtoNotary`, `getDtoProcedure`, and `getInvolvedPersonDto` stay on the entity. They are not the ratchet line.
- The baseline file becomes empty.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A management DTO still copies number, notary, procedures, distinct clients, and status | existing `getDto` | Made explicit |
| The entity does not declare `getDto` | #2329 | Changed |

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | mapper and the ratchet baseline |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD | no | |

### Surface area

- Endpoints: none
- Not **BREAKING**. No production caller used `DeedManagement.getDto`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Product docs | none |

## Out of Scope

- Moving `getDtoNotary` or `getDtoProcedure`.
