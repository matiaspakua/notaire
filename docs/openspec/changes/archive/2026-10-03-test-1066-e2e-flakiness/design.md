> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Audit #1066 (`audit-2026-09`): false-green E2E and sleep/retry masking.

Verified hotspots (as of Gate 1 draft):

- `TS-0021-workflow-editor-admin.spec.ts` — three `test.skip()` when
  `[data-testid^=btn-editor-]` count is 0
- `TS-0022-workflow-assignment-admin.spec.ts` — `test.skip()` when table has 0 rows
- Fixed sleeps: `TS-0040` (2× 2000 ms), `TS-0043` (2000 ms), plus helpers /
  demo suites (`gherkin-helpers`, `TS-0060`, `TS-0071`, `TS-0090`)
- Widespread `waitForLoadState("networkidle")` across workflows / QA specs
- `frontend/playwright.config.ts`: `retries: CI ? 2 : 0`, `timeout: 300000`
- Intentional skips without issue citations: TS-0014, TS-0016, TS-0017, TS-0020
- Good pattern already present: `setup/api-helpers.ts` (`createWorkflowDefinition`,
  nodes, transitions, `seedGestionWithWorkflow`) used by other suites

Fleet implements only after #1145 merges (serialize CI / avoid parallel Playwright churn).

## Goals / Non-Goals

**Goals:**

- TS-0021 / TS-0022 always arrange data; zero runtime skip for missing seed rows.
- No `waitForTimeout` in non-demo production workflow/QA specs named by #1066;
  replace with web-first assertions. Reduce unnecessary `networkidle` waits in
  touched files.
- Every remaining intentional `test.skip` includes an open issue number in the
  reason string.
- CI retries reduced to 1; flaky failures remain visible via retry traces /
  artifacts and mapping notes.
- Tighten default timeout where safe (keep long timeout only for demo/tour
  projects that need it).

**Non-Goals:**

- Closing product gaps behind intentional skips (unless arrange-only).
- Full suite rewrite of every `networkidle` call in one PR if risk is high —
  prioritize #1066-named files + helpers used by them; track residual as follow-up
  only if AC still met for named hotspots.
- Changing application production code except where a tiny testability hook is
  required (prefer API arrange + existing `data-testid`).

## Decisions

1. **Arrange via `api-helpers`, not UI-only setup for missing rows**
   - Why: AC requires tests arrange their own data; helpers already create
     workflows/nodes/transitions and tipo-tramite linkages.
   - TS-0021: before editor tests, create a workflow (and nodes if needed) so
     `btn-editor-*` exists.
   - TS-0022: ensure at least one tipo-tramite row exists (create via API) before
     asserting the workflow selector.

2. **Web-first waits; ban fixed sleeps in touched specs**
   - Why: `waitForTimeout(2000)` masks races and slows CI.
   - Prefer `expect(locator).toBeVisible()`, `toHaveURL`, `toHaveText`, response
     waits. For language switch (TS-0040), assert visible locale string/attribute
     change instead of sleeping.
   - Demo suites (TS-0071/TS-0090) may keep intentional paced pauses only if
     gated (e.g. `HEADED` / `SLOW_MO`) and not used as correctness waits in CI.

3. **Intentional skips must cite `#NNNN`**
   - Why: CONSTITUTION / CU76 forbid silent debt.
   - At implement: for each remaining skip in TS-0014/16/17/20, link an existing
     open issue or open a focused tracking issue and put `#id` in
     `test.skip(condition, '… #id')` / skip title.

4. **CI retries = 1; keep failure evidence**
   - Why: AC — “CI retries reduced to 1 and flaky reports tracked”.
   - Set `retries: process.env.CI ? 1 : 0` (and align smoke project if needed).
   - Tracking: retain Playwright HTML/JSON/JUnit + `trace: on-first-retry`;
     document in `E2E-TEST-MAPPING.md` that flake triage uses those artifacts
     (and CI run annotations), not silent green via retries.

5. **Timeout hygiene**
   - Why: 300 s global timeout hides hung tests.
   - Prefer lower default (e.g. 60–90 s) with explicit longer timeout on
     demo/tour specs only.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Arranging workflow data races with parallel workers | Unique names via `uniqueId()`; cleanup in teardown; avoid shared mutable seed |
| Removing `networkidle` reveals real races | Replace with specific locators; fix tests rather than re-add sleeps |
| Creating tracking issues for intentional skips | Prefer existing open issues from E2E-TEST-MAPPING debt table; create only if none |
| Parallel with #1145 Playwright changes | Gate: implement after #1145 merges; rebase onto main |
| Demo suites still use paced sleeps | Scope: CI correctness paths; gate demo pauses behind HEADED/SLOW_MO |

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| TS-0021 runs without data-missing skip | E2E | `TS-0021-workflow-editor-admin.spec.ts` |
| TS-0022 runs without empty-table skip | E2E | `TS-0022-workflow-assignment-admin.spec.ts` |
| No waitForTimeout in named QA hotspots | lint/static or focused grep gate in CI/preflight optional; primary = code review + E2E green | touched specs + helper |
| Intentional skips cite open issues | static check / review; document in mapping | TS-0014/16/17/20 |
| CI retries == 1 | config assertion (unit/doc) or review of `playwright.config.ts` | `playwright.config.ts` |
| Locale / icons assert without fixed sleep | E2E | `TS-0040`, `TS-0043` |

- New unit tests (`frontend/src/tests/unit/`): optional small helper tests if new
  arrange wrappers are extracted; otherwise E2E-first (this change is test infra)
- New integration tests (`src/test/java/.../integration/`): n/a
- Coverage impact (JaCoCo ratchet floor; 80% target): unchanged (no backend code)

TDD note: for test-infra changes, “failing first” means observe current false-green
/ skip behavior (or a temporary assertion that skips are zero / sleeps absent),
then implement arrange + wait fixes until green for real reasons.

## Regression Strategy

- Existing tests affected: TS-0021, TS-0022, TS-0040, TS-0043, possibly
  `gherkin-helpers.ts`, intentional-skip suites (metadata only),
  `playwright.config.ts` consumers in CI
- Full suite command: `cd frontend && npx playwright test` (stack up)
- Backend: `mvn test -pl backend-api` sanity only (no delta expected)
- HTTP/Bruno API suite: n/a
- Legacy paths at risk: suites that depended on global seed having workflows —
  after this change they self-seed

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  - Update `TS-0021-workflow-editor-admin.spec.ts` — arrange workflow via API
  - Update `TS-0022-workflow-assignment-admin.spec.ts` — arrange tipo-tramite
  - Update `TS-0040-l10n-language-switching-qa.spec.ts` — remove 2000 ms sleeps
  - Update `TS-0043-icons-ux-qa.spec.ts` — remove 2000 ms sleep; reduce networkidle
  - Annotate intentional skips in TS-0014/16/17/20 with `#issue`
  - Update `playwright.config.ts` retries/timeout
  - Update `E2E-TEST-MAPPING.md` accordingly
- Golden path: editor and assignment tests create data → exercise UI → pass without skip
- Edge / error paths: API arrange failure must fail the test (throw), never skip
- Viewports: unchanged unless a touched QA spec already covers them
- Command: `cd frontend && npx playwright test TS-0021 TS-0022 TS-0040 TS-0043`
  then full suite in CI

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none (test/CI only); no production deploy required
  for product behavior
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): n/a for product UI; Gate 5 = confirm CI
  Playwright job green with retries=1 and named suites not skipping

## Rollback Strategy

- Revert safe: yes — revert the PR; prior skips/sleeps return
- Database rollback: none needed
- Data written under the new behavior after revert: ephemeral E2E seed data only
- Blast radius if rollback is delayed: stricter E2E may fail more often (desired
  signal) — ops can temporarily raise retries only with an explicit exception

## Migration Plan

None beyond shipping the test/CI change after Gate 3/4. Copy this draft from
`/cursor/stores/self/internal/openspec-1066/` into
`openspec/changes/test-1066-e2e-flakiness/` when implement starts (after #1145).

## Open Questions

None blocking Gate 1. Implement may choose exact default timeout value
(60s vs 90s) and whether residual `networkidle` outside named files is a
same-PR cleanup or a follow-up issue — AC is satisfied when #1066 hotspots are fixed.
