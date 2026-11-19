# security: model testimony, folio, and submitted documents in LINDDUN

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2007 |
| Use Case | CU78 — Seguridad, Privacidad y Cumplimiento |
| Branch | `cursor/2007-linddun-records-47dc` |
| Gate 1 status | passed |

## Objetivo

LINDDUN named person fields and request logs. Testimony, folio, and submitted-document records were not in the model, and two rows still pointed at closed issue #309 via #2007.

## What Changes

- LD-7, LD-8, and LD-9 describe those records: stored fields, who can read them, and what the request log does not write.
- LD-9 stays Open: `@AuditPersonalDataRead` is still only on person reads.
- LD-4 and LD-5 point at the program section. They do not interpret a statute.
- The register lists the same LD ids.
- The open-work table drops #2007. It also drops #2005, which has been closed since the headers shipped.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Testimony and folio reads require the escrituras module | CU78, `ApiModulePathRegistry` | Made explicit |
| Submitted-document reads require the tramites module | CU78, `ApiModulePathRegistry` | Made explicit |
| Those reads are not an audit event | CU78, #1635 | Made explicit |
| Counsel owns statute reading | CU78 | Made explicit |

## Capabilities

### New Capabilities

- None. No API behavior changes.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Entities: described only. Testimony, folio, and submitted document columns are unchanged.
- Endpoints: none
- Not **BREAKING**.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` | LD-4, LD-5, LD-7, LD-8, LD-9 |
| `docs/200-architecture/206-security/threat-model/system.stride.yml` | the same LD ids |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | §7 and the open-work table |
| `docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md` | LINDDUN scope and the #2007 gap row |

## Out of Scope

- A privacy notice, a statute map, or a new read-audit annotation.
- A new GitHub issue for LD-9. The row is the open residual.
- Header values (#2005 is already closed).
