> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Issue #2070 (P1 Operate UI). Related #1368 motion tokens and #2073 reduced-motion MotionConfig. DataTable.tsx currently hardcodes rowTransition with 0.3s / Material ease / y:6 / Math.min(i*0.03, 0.3).

## Goals / Non-Goals

**Goals:** Align DataTable row/card entrance with motion tokens; lock with a failing-if-regressed unit test; leave reduced-motion behavior to MotionConfig.
**Non-Goals:** Table density redesign; Wilson/portal/Context Lake; committer required=true; changing MotionConfig.

## Decisions

1. **Reuse `staggerDelay` from `@/components/motion` and tokens from `@/theme/motion`.** Same helpers as FadeIn/fadeUpItem — no new stagger math in DataTable.
2. **Export `rowTransition` (and keep initial y on `motion.distance.sm`) for unit assertion.** Acceptance requires a test that fails if 0.3s / Material ease returns; exporting the helper is the cheapest lock without brittle DOM animation spies.
3. **No new MotionConfig changes.** Reduced motion already drops transforms (#2073).

## Risks / Trade-offs

- [Risk] Exporting rowTransition widens the public surface → Mitigation: export only the transition helper used by rows/cards; document as #2070 contract.
- [Risk] Stagger cap change (0.3 → 0.15) feels snappier on long lists → Mitigation: matches #1368 contract; intentional.
- [Risk] Source-scan tests elsewhere still allow y:6 if only helper is fixed → Mitigation: set initial y from tokens.distance.sm in both desktop and mobile paths.

## Testing Strategy

| Scenario | Test level | File |
|----------|------------|------|
| Row transition uses token duration/ease/staggerDelay | unit | `data-table-motion.test.ts` |
| Row rise uses distance.sm | unit | `data-table-motion.test.ts` |
| Hardcoded 0.3 / Material ease rejected | unit | `data-table-motion.test.ts` |

- New unit tests: `frontend/src/tests/unit/data-table-motion.test.ts`
- Integration / Bruno: n/a
- Coverage: frontend Vitest only

## Regression Strategy

- Existing DataTable tests keep rendering/empty/error contracts.
- Full suite: `bash workspace/sdlc/preflight.sh --full`
- HTTP/Bruno: n/a

## Playwright Strategy

n/a — no new interactive UI surface; Vitest locks the motion contract (MotionConfig already covers reduced motion).

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
