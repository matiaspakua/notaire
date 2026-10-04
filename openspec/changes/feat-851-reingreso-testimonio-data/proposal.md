# Capture cartón number, observation flag and notes when re-entering a testimony (CU44)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #851 |
| Use Case | CU44 – Reingresar testimonio (#197) |
| Branch | `feat/851_reingreso_testimonio` |
| Gate 1 status | draft |

## Objetivo

CU44 step 5 asks for the cartón number, whether the registry returned the testimony observed, and observations when it is re-entered. The current reenter endpoint only stamps the entry date. Persist the missing data and ask for it in the UI.

## What Changes

- Flyway `V42`: additive column `testimony_movements.observed_by_registry BOOLEAN NOT NULL DEFAULT false` (notes and folder_number already exist).
- `TestimonyMovement` and `DtoTestimonyMovement` carry `observedByRegistry`.
- `TestimonyMovementService.reenter` takes the cartón number, the observation flag and the notes, stores them on the new movement and rejects an observed reentry without notes.
- `POST /api/v1/movimiento-testimonio/{id}/reenter` accepts an optional `DtoTestimonyMovement` body (`cardNumber`, `observedByRegistry`, `notes`); an empty body keeps working.
- The testimony-movement screen opens a dialog asking for those fields before re-entering; `useReingresar` posts them.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A reentry flagged as observed by the registry MUST carry non-blank notes | CU44 step 5 | Made explicit |
| A reentry records the cartón number and notes on the new movement, leaving the previous movement unchanged | CU44 step 5-7 | New |

## Capabilities

### New Capabilities

- `testimony-reentry-data`: Record the data CU44 requires when a withdrawn testimony is re-entered.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | migration V42, entity, DTO, service, controller |
| `frontend` | yes | reentry dialog, hook, types, i18n |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | notaire-shared DTO, CU44 doc, E2E spec |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Additive schema change and a request body on an existing endpoint. No ADR; follows ADR-007 (Flyway only).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU44 – Reingresar testimonio.md` | traceability to the implemented data |
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | regenerated with the new column |
| `CHANGELOG.md` | one entry |
