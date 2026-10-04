# Bring CU84 to the standard Use Case template and fix the requirements CSV

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #956 |
| Use Case | CU84 – Login al sistema |
| Branch | `docs/956_cu84_template` |
| Gate 1 status | draft |

## Objetivo

CU84 uses a different template (no cross-references, no GitHub ID) and requerimientos.csv ends with two malformed rows for the same requirement. Fix both so the RF-CU-Issue cross-check works, and guard it.

## What Changes

- `CU84 - Login.md` rewritten on the standard template: information table with Referencias Cruzadas (RF #82 and the new requirement issue) and GitHub ID, Curso de Eventos table, Excepciones / Flujos Alternativos, Postcondiciones. Behaviour described is unchanged.
- `requerimientos.csv`: the two malformed rows become one `#1224,Login al sistema,...` row; #1224 is the requirement-tracking issue created for it.
- `scripts/test_business_docs_traceability.py` (with a CI wrapper) fails on a malformed requirement row, a duplicated ID or title, or a Use Case file without the Referencias Cruzadas and GitHub ID rows.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every requirement row carries a real GitHub ID; every Use Case has cross-references and GitHub IDs | #956, Constitution P4 | Made explicit |

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
| Docs / scripts / CI | yes | business docs, one guard |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Documentation only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU84 - Login.md` | migrated to the template |
| `docs/100-business/101-requirements/requerimientos.csv` | single valid Login row |
| `CHANGELOG.md` | one line |
