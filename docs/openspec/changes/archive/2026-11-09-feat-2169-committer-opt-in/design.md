> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Adopt #2167 without failing greenfield agent PRs.

## Goals / Non-Goals

**Goals:** no-evaluable until runtime_* set; opt-in gate; docs.
**Non-Goals:** Enforcing required=true; Isolator.

## Decisions

1. `CommitterPolicy.evaluable()` requires non-empty `runtime_names` or `runtime_emails`.
2. Agent deny lists may be pre-seeded; they do not activate the check alone.
3. Notaire gate: `required = false` on `pre-pr` (or merge) calling `aisdlc committer --base {base}`.

## Riesgos / Trade-offs

None — stricter activation.

## Testing Strategy

Update CommitterPolicyTest; add workspace wiring test.

## Regression Strategy

Existing committer scenarios keep runtime in fixtures.

## Playwright Strategy

n/a

## Deployment Strategy

Docs + profile.

## Rollback Strategy

Revert PR.

## Migration Plan

None.

## Open Questions

None.
