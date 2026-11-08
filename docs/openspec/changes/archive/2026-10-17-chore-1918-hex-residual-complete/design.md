> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

ArchUnit R1–R6 empty on main. Residual `jpa/` (~36) and `service/` (4) remain.
Wholesale delete is #576. This slice removes dead service types and moves
`PersistenceConstants` so `business` no longer imports `jpa`.

## Goals / Non-Goals

**Goals:** Safe residual shrink; truthful permanent docs; keep R1–R6 empty.
**Non-Goals:** Delete all JpaControllers; close FE issues (bot 403).

## Decisions

1. `PersistenceConstants` → `com.licensis.notaire.persistence` (not under legacy).
2. Delete unused SessionAdministrator / ValidationAdministrator.
3. ADR documents Owner-accepted residual: keep JpaAdministrator + LegacyJpaEmfConfig + jpa controllers until #576.

## Riesgos / Trade-offs

INITIAL_VERSION is a compile-time constant (0); move is package-only, no behaviour change.

## Testing Strategy

PersistenceConstantsTest (new package) + HexagonArchitectureTest + compile.

## Regression Strategy

Existing Folio/Notebook entity versioning; jpa controller optimistic-lock paths.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
