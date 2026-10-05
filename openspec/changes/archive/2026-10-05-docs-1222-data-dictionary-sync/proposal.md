# Regenerate the Diccionario de Datos from the migrated schema and guard against drift

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1222 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `docs/1222_data_dictionary` |
| Gate 1 status | draft |

## Objetivo

The dictionary lists Spanish column names for 29 of 36 tables and omits four tables. Derive its structural content from the schema Flyway builds, keep the human descriptions, and fail CI when the dictionary and the schema diverge again.

## What Changes

- `scripts/generate_data_dictionary.py` rewrites the index (section 2), the per-table column tables (section 4) and the referential matrix (section 5) from the migrated database; descriptions are read from the existing file and kept, undocumented items get a `TODO` marker.
- `scripts/generate_erd.py` loader also returns column defaults, lengths and ON DELETE actions so both generators share one schema reader.
- `Diccionario de Datos.md` regenerated: 36 tables, current column names, types, nullability, defaults and references; the ERD CSV `Observaciones` column is repopulated from it.
- `scripts/test_data_dictionary_sync.py` (with a CI wrapper) compares the dictionary with the committed ERD artifacts: tables, columns, PK, FK and nullability, foreign-key matrix, and absence of `TODO`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The Diccionario de Datos structural content equals the Flyway schema | #1222, Constitution P4 | Made explicit |

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
| Docs / scripts / CI | yes | docs generator, one guard |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Documentation tooling only. No ADR; follows ADR-007 (Flyway is the schema source of truth).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | regenerated |
| `docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv` | Observaciones repopulated |
| `CHANGELOG.md` | one line |
