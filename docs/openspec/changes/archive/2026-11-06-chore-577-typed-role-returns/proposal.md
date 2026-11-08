# Type RoleController create/assign returns via advice (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `chore/577_typed-role-returns` |
| Gate 1 status | passed |

## Objetivo

`RoleController#createRole` and `#assignRoleToUser` declare `ResponseEntity<Object>` because of catch Map/empty 404 bodies.

## Why

Issue #577 typed returns + thin controllers via `RoleExceptionHandler`.

## What Changes

- Add `RoleExceptionHandler` for RoleNameConflict / RoleNotFound / UserNotFound
- `createRole` → `ResponseEntity<RoleResponse>`; `assignRoleToUser` → `ResponseEntity<Map<String, Integer>>`
- Remove try/catch from RoleController methods
- Shrink baseline by two

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Role create/assign HTTP shapes unchanged via advice | Issue #577 | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, handler, tests, baseline |

### Surface area

- Same 201/409/404 and assign JSON body

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note (tracking issue 577) |

## Out of Scope

["Closing #577"]
