> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2134. Related #2023, Constitution P12, Warp/Port self-improve themes.

## Goals / Non-Goals

**Goals:** Smallest useful loop: evidence → proposal → gated PR path; Wilson score; no agent commit/approve.
**Non-Goals:** Kit opening forge PRs; autonomous Constitution merge; silent flag weakening; dual-LLM self-approval.

## Decisions

CLI improve propose/validate/autonomy; proposals dir under evidence parent; stdlib math only; omit CHANGELOG while Release Please #2150 holds fence.

## Riesgos / Trade-offs

Operators may treat proposal as applied change — document commits=false and validate gates.

## Testing Strategy

test_improve.py for propose/validate/wilson

## Regression Strategy

protected, ratchet, evidence report tests

## Playwright Strategy

n/a — kit CLI, not product UI

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
