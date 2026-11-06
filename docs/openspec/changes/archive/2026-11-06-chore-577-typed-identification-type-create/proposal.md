# Type IdentificationType#create return (shrink #577 loose baseline)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU76 |
| Branch | `chore/577_typed-identification-type-create` |
| Gate 1 status | passed |

## Objetivo

`IdentificationTypeController#create` still declares `ResponseEntity<Object>` while update/delete are already `Void` and advice maps domain exceptions.

## Why

Issue #577 ratchet; no wire-shape change.

## What Changes

- `IdentificationTypeController#create` → `ResponseEntity<IdentificationTypeResponse>`
- Remove `IdentificationTypeController#create` from `controller-loose-returns.txt`
- Document that linked git worktrees need their own `.env` for `preflight.sh --full` docker compose smoke (`JWT_SECRET`)

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| IdentificationType create uses a typed response body. | Issue #577; CU76 | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

### ADDED via delta

- `controller-typed-returns`: IdentificationType create leaves the Object loose baseline.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller + baseline |
| `docs` | yes | Environment checklist worktree `.env` note |

### Surface area

- Endpoint: same HTTP status and body

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note (tracking issue 577) |
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | Worktree `.env` copy |

## Out of Scope

["Closing #577", "Other loose baseline rows"]
