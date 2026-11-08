# R6 clear — no non-legacy deps on jpa/service

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1914 |
| Use Case | CU / architecture (epic #1693 / ADR-030) |
| Branch | `cursor/fix-1914-r6-clear-69d3` |
| Gate 1 status | passed |

## Objetivo

Clear ArchUnit **R6** (2 edges): `IdentificationTypeLookup→IdentificationTypeJpaController` and `JpaConfig→JpaAdministrator`. R2 already 0 on main.

## What Changes

Wire IdentificationTypeLookup catalog from JpaAdministrator (remove jpa import from business); move EMF bootstrap to `service.LegacyJpaEmfConfig` and delete `config.JpaConfig`; empty hexagon-baseline (R1–R6).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Packages outside `jpa`/`service` must not depend on those legacy packages | #1914 / ADR-030 R6 | Made explicit by clearing baseline |

## Capabilities

### New Capabilities
- `r6-legacy-deps`: R6 baseline empty; legacy packages gain no external dependents

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | lookup wiring; move EMF config; empty baseline |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none (Spring still boots EMF via renamed `@Configuration`)
- Dependencies: none

### Architecture review

Follows ADR-030 rule 8; no new ADR (slice log only).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | R6 clear slice log |
| `docs/200-architecture/202-ADR/ADR-002-module-structure.md` | JpaConfig → LegacyJpaEmfConfig path |
| `CHANGELOG.md` | n/a — omit Unreleased |

## Out of Scope

Deleting empty legacy packages; FE hex; CHANGELOG Unreleased; behaviour changes
