> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #976 (bug, FRONTEND, DEVOPS, TEST, audit-2026-09, priority:medium, CU76).

**Historical failure (issue body):** on 2026-09-07/08 `main`, Vitest reported
branches **5.85%** vs threshold **6%**, failing Frontend CI even for
backend-only PRs.

**Prep measurement (2026-10-03, `origin/main` worktree at `9642a033`):**

```text
Statements : 12.87%
Branches   : 8.61%
Functions  : 10.9%
Lines      : 13.25%
```

`npx vitest run --coverage` exit 0. Recent Frontend CI runs on `main` are
`success`. Config still has thresholds statements 10 / branches 6 / functions 9
/ lines 10 with comments dated 2026-07-29.

**Root-cause conclusion (Gate 1):** The 6% branch floor was set as a low
aspirational ratchet with headroom (comment in `vitest.config.ts`). Coverage
later dipped under that floor (denominators grew faster than tests / untested
branches added), producing persistent CI red. Subsequent frontend unit tests
(auth, admin-access, hooks, etc.) raised measured branches above 6% again. The
remaining gap vs #976 AC is **policy intentionality**: raise floors toward
current reality and document raise-only rules so CI cannot go false-green while
floors rot, and cannot be silently lowered.

## Goals / Non-Goals

**Goals:**

- Document root cause.
- Ratchet thresholds up with headroom under measured coverage.
- Keep Frontend CI Vitest green.
- Mirror JaCoCo raise-only documentation for frontend.

**Non-Goals:**

- Reaching 80% frontend coverage in this change.
- Changing Playwright or backend JaCoCo floors.
- Mass-writing unrelated unit tests beyond what is needed if measurement dips
  again at implement time.

## Decisions

1. **Prefer raising floors over lowering** — never solve a red build by cutting
   thresholds without ADR/exception.
2. **Re-measure on implement** — use `npx vitest run --coverage` on updated
   `main`; set e.g. branches to `Math.floor(measured) - 1` style headroom
   (prep suggestion: branches 8, statements 12, lines 12, functions 10 — adjust
   if measurement moves).
3. **Optional static guard** — a tiny unit/script test that reads
   `vitest.config.ts` thresholds and asserts they are ≥ the documented floor
   constants committed beside the policy (prevents accidental lower).
4. **If measurement somehow regresses under 6% again** — add targeted branch
   tests first; only then consider temporary floor with documented exception.

## Riesgos / Trade-offs

- [Aggressive ratchet] → flaky CI on small refactors — Mitigation: keep ≥1
  percentage point headroom.
- [Docs-only without ratchet] → fails #976 AC spirit — Mitigation: must bump
  thresholds.
- [Closing issue as “already green” without docs] → recurrence risk — Mitigation:
  docs + ratchet mandatory.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Root cause documented | docs checklist | PR / permanent docs |
| Coverage run passes with raised floors | unit (vitest coverage) | `npm run test:coverage` |
| Floors below measured | manual/assert in PR notes + optional script | design checklist |
| Frontend CI green | CI | `frontend-ci.yml` |
| Policy documented | docs checklist | code-quality.md |

- TDD angle: if adding a threshold-guard test, write it expecting the new floor
  constants before bumping config (or bump config in same red→green cycle with
  observed coverage command).
- JaCoCo: n/a

## Regression Strategy

- Existing Vitest suite must remain green.
- Do not weaken product asserts to gain coverage.
- Full frontend: `npm test`, `npm run test:coverage`, lint, typecheck.

## Playwright Strategy

- n/a — no UI product surface; coverage tooling/docs only.
- PR still subject to required CI jobs.

## Deployment Strategy

- Merge via PR; no runtime deploy behavior change.

## Rollback Strategy

- Revert PR restores prior thresholds/docs. Prefer forward-fix (add tests) if
  coverage dips after unrelated merges rather than lowering floors.
