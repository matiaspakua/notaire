> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`npx vitest run --coverage` on this branch's base (`770f642b`, 2026-11-17) reported statements 89.15%, branches 81.43%, functions 87.99%, lines 90.46%. Enforced floors were 86 / 79 / 85 / 88. Integer floors that keep at least two points of headroom are 87 / 79 / 85 / 88. Only statements move. The Constitution, TEST-PLAN and testing README still quote the October floors 76 / 68 / 72 / 78.

## Goals / Non-Goals

**Goals:**

- Raise the statement floor by one point and keep the other three floors.
- Make the guard test and the three permanent floor tables name 87 / 79 / 85 / 88.
- Record the 2026-11-17 measurement next to the floors in the quality rule.

**Non-Goals:**

- A floor of 90 on any metric.
- New page or hook tests.
- A JaCoCo change.

## Decisions

Keep two points of headroom, the same gap the config comment and #1663 already use. One point would allow lines 89 and functions 86, and a small coverage wobble would turn `main` red. Two points allow only the statement floor to rise.

The guard test reads the config and the three permanent documents. A later edit that updates only the config, or only a doc, fails the unit test.

## Riesgos / Trade-offs

- [A later test deletion drops statements under 87] → the floor is one point, with 2.15 points measured headroom (about 136 statements). The guard fails in that PR instead of silently lowering the floor.
- [Documents outside the three tables still mention 76/68/72/78] → archived OpenSpec specs are historical "at least" minimums and stay. The live tables this change names are the ones agents read first.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Guard rejects a statement floor below the ratchet | unit | `frontend/src/tests/unit/vitest-coverage-thresholds.test.ts` |
| Permanent docs quote the enforced floors | unit | same file, reading the three markdown sources |

- New unit tests (`src/test/java/.../unit/`): none
- New integration tests (`src/test/java/.../integration/`): none
- Coverage impact (JaCoCo raise-only floor; Owner >90% target #2046): no JaCoCo change. Vitest statement floor 86 → 87.

TDD: raise the documented floors in the guard test first and observe the config assertion fail, then set the config and the documents.

## Regression Strategy

- Existing tests affected: `vitest-coverage-thresholds.test.ts` expectations move up. No assertion is removed.
- Full suite command: `cd frontend && npx vitest run --coverage`
- HTTP/Bruno API suite: not affected
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. Thresholds and documentation only.

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: not run for this change

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `cd frontend && npx vitest run --coverage` exits 0

## Rollback Strategy

- Revert safe: yes. Reverting restores the statement floor of 86 and the previous doc text. No data is written.
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: frontend CI stays one point stricter until revert

## Migration Plan

One pull request. No staged rollout.

## Open Questions

None. The two-point headroom rule decides which floors move.
