> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Notaire factory validation report follow-on.

## Goals / Non-Goals

**Goals:** Stop empty proposal noise.
**Non-Goals:** Changing validate/autonomy.

## Decisions

Match autonomy: exit 0 with status no-evaluable; omit CHANGELOG while Release Please #2150 holds fence.

## Riesgos / Trade-offs

None — stricter emptiness handling.

## Testing Strategy

ImproveProposeTest empty ledger case

## Regression Strategy

existing propose-with-friction test

## Playwright Strategy

n/a

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
