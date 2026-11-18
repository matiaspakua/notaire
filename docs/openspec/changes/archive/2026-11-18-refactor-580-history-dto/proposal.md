# refactor: delete unused History.getDto

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-history-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`business.History.getDto` built the legacy `DtoHistory`. No production or test caller uses it. The history API uses `HistoryMapper` and `DtoHistorySummary`. This slice deletes the unused method and shrinks the ratchet.

## What Changes

- Delete `History.getDto` and its DTO imports.
- Remove `History#getDto` from the ratchet.
- Add a test that the entity has no `getDto`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| History responses stay `DtoHistorySummary` | existing `HistoryMapper` | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Delete unused entity method |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` | no | |
| CI/CD | no | |

### Surface area

- Endpoints: none
- Not **BREAKING** for API clients. `DtoHistory` remains for `DtoDeedManagement.recordHistory`.

### Write set

- `History.java`
- `HistoryDtoRemovalTest.java`
- `entity-dto-mapping.txt`

### Architecture review

Follows ADR-030. The adapter mapper already exists. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Factory comparison | this locate |
| Product docs | none |
| GitHub Pages | none |

## Out of Scope

- `DeedManagement.getDto`.
- Paging lists.
