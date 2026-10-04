# Make the E2E gestión helper send the notary field the API reads

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1236 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `test/1236_gestion_helper_notary` |
| Gate 1 status | draft |

## Objetivo

`createGestionSinTramite` sends `fkIdNotaryPerson`, which the API ignores, so the stored gestión has no notary and JPA cannot load it. Fix the helper and guard against the stale field name.

## What Changes

- `createGestionSinTramite` sends `notaryPersonId`.
- `scripts/test_e2e_reliability.py` fails when any E2E source sends `fkIdNotaryPerson` to the API.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| E2E helpers send only fields the API reads | #1236 | Made explicit |

## Capabilities

### New Capabilities

- (none — `skip_specs: true`)

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | E2E helper, one static guard |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Test infrastructure only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one entry |
