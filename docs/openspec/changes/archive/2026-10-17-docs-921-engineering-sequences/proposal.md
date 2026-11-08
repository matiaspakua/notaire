# Sequence the testimony and document re-entry the services already run

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #921 |
| Use Case | CU44 – Reingresar testimonio |
| Branch | `cursor/engineering-doc-gaps-853b` |
| Gate 1 status | passed |

## Objetivo

The engineering catalog is present (`DOC-ING`, `DOC-CICLO`, `DOC-SAD`,
`DOC-MTP`). The case schema omitted two sequences the API already executes,
and its testimony path skipped verification and inscription.

## What Changes

- `DOC-GESTION` §3.4 verifies before entry and records inscription before
  withdrawal.
- §3.7 sequences testimony re-entry (CU44). §3.8 sequences document re-entry
  (CU43). Neither moves the management.
- `DOC-ING` identifies `DOC-GESTION`. `EVALUACION-INGENIERIA.md` records what
  is available and which gaps are not a missing file.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Entry requires a verified testimony | CU08, CU11 | Made explicit |
| Withdrawal requires a registered movement | CU11, CU12 | Made explicit |
| Re-entry creates a new movement and does not move the management | CU44 | Made explicit |
| Document re-entry creates a submitted document and does not move the management | CU43 | Made explicit |

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
| `docs` | yes | Case sequences, engineering catalog, indexes |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No new ADR. States stay in `DOC-CICLO`. ADR-027 stays the diagram language.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/203-design/GESTION.md` | Verify, inscription, both re-entries |
| `docs/300-development/EVALUACION-INGENIERIA.md` | Catalog and remaining gaps |
| `docs/300-development/INGENIERIA.md` | `DOC-GESTION` id |
| `CHANGELOG.md` | Unreleased note |

## Out of Scope

A service for Anulada, No Paso, or Inscripta. A return from Observada.
Regenerating the data dictionary (#1222). Closing #921.
