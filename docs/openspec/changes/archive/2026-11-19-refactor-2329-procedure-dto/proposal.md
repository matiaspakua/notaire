# refactor: move procedure transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2329 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2329-procedure-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`Procedure.getDto` built `DtoProcedure`, including a short deed, management, property, and budget. The deed and testimony mappers called it. This slice moves that copy off the entity.

## What Changes

- Add `ProcedureLegacyMapper.toDto`.
- The deed and testimony mappers call it.
- Delete `Procedure.getDto`.
- Drop that ratchet line.
- Record the Notaire refresh measured on this sampled locate.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Optional deed, management, property, and budget are copied only when present | existing method | Made explicit |
| Building the DTO replaces the entity's submitted-document list with an empty list | existing method | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper, Procedure, two callers |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` | no | |
| CI/CD | no | |

### Surface area

- Endpoints: none
- Not **BREAKING** for API clients.

### Write set

- `ProcedureLegacyMapper.java`
- `Procedure.java`
- `DeedLegacyMapper.java`
- `TestimonyLegacyMapper.java`
- `ProcedureLegacyMapperTest.java`
- `ProcedureEntityTest.java`
- `entity-dto-mapping.txt`
- factory KPI notes

### Architecture review

Follows ADR-030. `getDtoPerson` and `setDtoDocument` stay on the entity; they are not on the ratchet. This is a sampled locate.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Product docs | none |
| factory-kpis.json | second refresh observation |

## Out of Scope

- `DeedManagement#getDto`. #2329 stays open.
