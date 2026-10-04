# Regenerate the ERD artifacts for the English schema

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1021 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure (documentation debt of epic #973) |
| Branch | `docs/1021_regenerate_erd` |
| Gate 1 status | draft |

## Objetivo

The data dictionary was updated alongside epic #973, but the ERD sources and renders still show the retired Spanish table names. Regenerate them from the schema Flyway actually builds, and guard them against drifting back.

## What Changes

- `scripts/generate_erd.py` reads the live PostgreSQL schema (migrated by Flyway) and rewrites `ERD.puml`, `ERD-Escribania_completo.puml` and `Modelo Relacional Escribania - Entidades.csv` with the current table and column names.
- `ERD_Escribania.svg` and `ERD_Escribania_completo.svg` are re-rendered from the sources with PlantUML.
- `scripts/test_erd_current_schema.py` (with a CI wrapper) fails when an artifact uses a retired table name or the three artifacts disagree.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The ERD describes the schema Flyway builds, with English names | #973, #1021, Constitution P6 | Made explicit |

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
| Docs / scripts / CI | yes | docs, one generator script, one guard |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Documentation only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/205-data-model/ERD/*` | regenerated |
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | drift against the schema listed, not rewritten here |
| `CHANGELOG.md` | one line |
