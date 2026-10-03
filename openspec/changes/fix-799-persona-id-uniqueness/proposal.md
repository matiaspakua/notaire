# Enforce Person identification type+number uniqueness at DB

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #799 |
| Use Case | CU17 — Dar Alta persona; CU18 — Dar Alta Cliente |
| Branch | `cursor/fix-799-persona-id-uniqueness-69d3` |
| Gate 1 status | passed |

## Objetivo

Issue #835 already rejects duplicate identification type+number in
`PersonService` with HTTP 409, but PostgreSQL still allows a second row under
concurrent inserts or any path that bypasses the service check. Without a
unique constraint on `people`, budgets, managements, and payments can still
silently split across two unlinked identities (CU17 / CU18 residual of #799).

## What Changes

- Add Flyway migration `V40` that fail-fast pre-checks duplicate groups, then
  creates a unique index on
  `people (fk_id_tipo_identificacion, identification_number)`.
- Align the JPA `@Table` unique constraint with that composite key.
- Optionally map race-condition `DataIntegrityViolationException` on that
  constraint back to `DuplicatePersonException` → HTTP 409 with
  `existingPersonId` so the #835 frontend toast path keeps working.
- Prove with a failing-then-green integration test that a second insert is
  rejected at the database.

**BREAKING CHANGES:** None for well-behaved clients. Environments with existing
duplicate type+number rows must clean them before migration (fail-fast message).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| No two people may share the same identification type and number (DB-enforced) | CU17, CU18, #835 | Made explicit at persistence |
| Uniqueness key is composite type+number (not number alone) | #835 / `persona-validacion-duplicados` | Made explicit in schema |
| Service-layer 409 remains the primary UX path; DB constraint is the last line of defense | CU17 / #835 | Made explicit |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `persona-validacion-duplicados`: require a database unique constraint matching
  the existing service-layer type+number rule, with migration pre-check and
  race-safe 409 mapping.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Flyway V40, `Person` uniqueConstraints, optional race mapping, integration tests |
| `frontend` | no | Confirm #835 409 toast path unchanged — no UI rebuild |
| `frontend-swing` | no | Removed; out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Person` / table `people` — unique index/constraint
- Endpoints: none new; create/update still return 409 on duplicate
- Database (Flyway `V40`): unique index on
  `(fk_id_tipo_identificacion, identification_number)`
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING:** no API contract change; migration fails if duplicate groups exist

### Architecture review

Follows Flyway-as-source-of-truth (Constitution P6) and existing repository/
service layering. No ADR required — additive uniqueness constraint only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU17 – Dar Alta persona.md` | Note DB-level uniqueness on type+number |
| `docs/100-business/102-use-cases/CU18 – Dar Alta Cliente.md` | Same residual uniqueness note if applicable |
| `CHANGELOG.md` | Fixed/Added entry under `[Unreleased]` for DB unique constraint |
| `openspec/specs/persona-validacion-duplicados/spec.md` | Sync after merge/archive |

## Out of Scope

- Rebuilding #835 happy path (service `DuplicatePersonException` → 409 + FE toast).
- Frontend changes beyond confirming the existing 409 path still works.
- Renaming `fk_id_tipo_identificacion` to English (deferred to rename ADR).
- Automatic merge/cleanup of historical duplicate people beyond fail-fast or
  lowest-`id` cleanup agreed in the migration design.
