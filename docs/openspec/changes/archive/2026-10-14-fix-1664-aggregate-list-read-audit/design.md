> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`AuditAspect` audits annotated reads through `PersonalDataReadDescriber`. UI pickers call the list endpoint repeatedly; each call became a row.

## Goals / Non-Goals

**Goals:** a readable trail; a stable audit pagination test; no lost first read.
**Non-Goals:** multi-instance window sharing; auditing other entities.

## Decisions

1. **Aggregate only the list handler** (opt-in attribute). Searches by DNI or name and lookups by id are the sensitive accesses and stay one row each.
2. **Per-user, per-instance window** held in memory. Another instance may add one row, but a user's first read of a window is always recorded.
3. **Default 60 s**, configurable, so an operator can tighten it.

## Riesgos / Trade-offs

- [A burst of list reads inside a window is invisible] → the trail still shows who listed and when; the individual rows were picker noise.
- [Memory growth of the per-user map] → one entry per user, replaced on each window.

## Testing Strategy

`AuditPersonalDataReadTest` gains four cases (written first, red), plus the integration test still shows one row per list read. `TS-0104` run repeatedly on the native stack.

## Regression Strategy

`mvn verify -pl backend-api`; Playwright `chromium` and `TS-0104` x5 through `workspace/stack/native.sh`.

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
