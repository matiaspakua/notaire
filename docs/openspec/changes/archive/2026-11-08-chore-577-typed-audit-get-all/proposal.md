# Type AuditRecordController#getAll (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU23 |
| Branch | `refactor/577_typed-audit-get-all` |
| Gate 1 status | passed (local) |

## Objetivo

`AuditRecordController#getAll` uses `ResponseEntity<?>`. This tip types the handler while keeping flat-list and paginated JSON shapes.

## What Changes

- `getAll` returns `ResponseEntity<AuditRecordGetAllResponse>` with `@JsonValue` serialization.
- `AuditRecordPageResponse` replaces the ad hoc `Map` page body.
- Shrink baseline by `AuditRecordController#getAll` only.

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: `AuditRecordController#getAll` leaves the loose-return baseline

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Unpaginated GET returns a flat DTO array | CU23 | Unchanged |
| Paginated GET returns `content`, `page`, `size`, totals | CU23 | Unchanged |

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Audit list return types, baseline |

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/audit/*`
- `backend-api/src/test/resources/architecture/controller-loose-returns.txt`
- `CHANGELOG.md`
- `docs/openspec/**`

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased, Refs #577 |
| `docs/openspec/specs/controller-typed-returns/spec.md` | audit getAll requirement |

## Out of Scope

- Closing #577
- `AuxiliaryProtocolController#startDeed`
