# Type PropertyController#create return (shrink #577 loose baseline)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `chore/577_typed-property-create` |
| Gate 1 status | passed |

## Objetivo

`PropertyController#create` still declares `ResponseEntity<Object>` while update/delete are already `Void` and `PropertyExceptionHandler` maps domain exceptions.

## Why

Issue #577 ratchet of typed API returns with no wire-shape change for clients.

## What Changes

- `PropertyController#create` → `ResponseEntity<PropertyResponse>`
- Remove `PropertyController#create` from `controller-loose-returns.txt`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Property create uses a typed response body. | Issue #577; CU76 | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller + baseline |

### Surface area

- Endpoint: same HTTP status and body

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note (tracking issue 577) |

## Out of Scope

["Closing #577", "Other loose baseline rows"]
