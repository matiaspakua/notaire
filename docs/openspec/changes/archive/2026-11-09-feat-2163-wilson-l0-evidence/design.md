> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

#2159 feeds `signal` into the ledger. Autonomy still reports L3 and counts stage `ok`.

## Goals / Non-Goals

**Goals:** Honest L0 Wilson grade from required `signal` gates; demote to L1 on incident.
**Non-Goals:** Higher levels, Isolator, forge attestations.

## Decisions

1. Only `stage == "signal"` runs contribute (preflight feed). No signal → no-evaluable.
2. Count gates where `required` is not false and status is `pass` or `fail`; exclude skip/todo.
3. Cap reported level at 0 when held/ok; level 1 only when demoted. Never emit 2 or 3.
4. Keep `min_n=30` and `WILSON_FLOOR=0.80`.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Existing test expects level 3 | Update to L0-eligible; document change |
| Sparse signal history | no-evaluable until N≥30 — honest |

## Testing Strategy

Update `AutonomyWilsonTest`; add skip≠pass and signal-only cases.

## Regression Strategy

Propose/validate tests unchanged; Wilson math tests unchanged.

## Playwright Strategy

n/a — kit CLI only

## Deployment Strategy

Ships with next release; local ledger only.

## Rollback Strategy

Revert the PR.

## Migration Plan

None — first honest grade after enough `signal` runs.

## Open Questions

None.
