# Assess the case documentation and require traceability to name its documents

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #921 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/business-docs-assessment-853b` |
| Gate 1 status | passed |

## Objetivo

The case process, which is the core of the system, did not have one analysis
and design schema, and the activity diagram contradicted the seeded workflow.
Each implementation also needs a mechanical check that the documents its
traceability names are in the same diff.

## What Changes

- Add the assessment and the case-process schema (`GESTION.md`).
- Rewrite the legacy activity so archive leaves Documentación completa and
  Testimonio Retirado is final. Correct the create-gestión sequence to
  `POST /api/v1/managements/complete-case` and `deed_managements`.
- Point the SAD, the lifecycle doc, and the indexes at that schema.
- Constitution §8, `INGENIERIA.md`, and the fleet docs require the named
  documents. `check-doc-traceability.py` enforces it on the diff.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A gestión moves only on a seeded edge; archive leaves Documentación completa | CU83, CU16 | Made explicit |
| Signing a deed does not move the gestión | CU06 | Made explicit |
| An implementation updates the permanent documents its traceability names | CU76, Constitution §8 | New |

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
| `docs` | yes | Assessment, case schema, lifecycle paths, SAD, indexes |
| CI/CD (`.github/workflows`) | yes | Process Checks run the traceability check |
| `workspace` | yes | `check-doc-traceability.py`, preflight, unit test |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No new ADR. ADR-014 remains the workflow decision. ADR-027 remains the diagram
language. The SAD runtime view points at the case schema.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/EVALUACION-DOCUMENTACION.md` | New assessment |
| `docs/200-architecture/203-design/GESTION.md` | New case schema |
| `CICLO-DE-VIDA.md`, SAD, indexes, Constitution §8 | Links and the traceability rule |
| `CHANGELOG.md` | Unreleased note |

## Out of Scope

Rewriting every use-case file. Closing #921 (the data dictionary and the
license remain). A dedicated sequence for testimony re-entry.
