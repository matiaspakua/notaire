> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1192, phase 2 of #1190, Use Case CU76. Assessment on `main` (2026-10-03):

| Finding | Detail |
|---------|--------|
| Suite | 58 tracked files under `frontend/tests/e2e` (52 `*.spec.ts`, helpers, `setup/`, `reporters/`); 530 passed, 0 failed, 14 skipped on the last full pipeline run |
| Coupling to frontend source | none: the only non-relative imports are `@playwright/test` (56), `node:fs` (2) and `@playwright/test/reporter` (1); no relative import leaves the tree |
| Path assumptions | working directory is `frontend/`: `testDir ./tests/e2e`, `globalSetup`, reporter path, `tests/e2e/fixtures/admin-auth.json` (storage state) and `tests/e2e/fixtures/e2e-admin-token.txt` |
| Dependencies | `playwright` (a runtime dependency, imported nowhere) and `@playwright/test` (dev) in the frontend; resolved 1.63.0 |
| Hidden consumers in `frontend/` | `src/tests/unit/e2e-test-reliability.test.ts` (static assertions on the E2E tree and config), `vitest.config.ts` exclude, `eslint.config.mjs` ignores and a `tests/**` rule block, `.gitignore`, `.dockerignore`; `tsconfig.json` includes `**/*.ts`, so the frontend currently type-checks and lints the suite |
| Gates | `playwright-e2e.yml` (`e2e-tests` job named `UI E2E Tests (Playwright)`, `suite-playwright-e2e` job named `Playwright E2E`, required by the `protect-main` ruleset), `preflight.sh --full`, `run_pipeline.sh` dashboard link, `generate_e2e_coverage_report.py` (finds `results.json` by search) |
| References | 33 active files, plus `CONSTITUTION.md` (§3, §4, §5 step 15, §7, §13) |

## Goals / Non-Goals

**Goals:** the same suite, in `testing/e2e`, with its own packaging, type-check and lint; every gate
and document repointed; identical results.

**Non-Goals:** changing a test; the Constitution amendment (#1210); wiring older guards (#1209);
the real repository split.

## Decisions

1. **`git mv frontend/tests/e2e testing/e2e/tests`** and the config to `testing/e2e/`. Moving the tree
   as a unit keeps every relative import valid (`./setup/...`, `../gherkin-helpers`), so no spec is edited.
   Only the config and three string constants change. The move is its own commit, with no content edits.
   - Rejected: flattening the tree under `testing/e2e/` — rewrites imports in 52 specs for no gain.
2. **Own `package.json` and lockfile** with `@playwright/test`, `typescript`, `eslint`, `typescript-eslint`
   and `@types/node`, at the same version ranges as the frontend. Playwright resolves to the same 1.63.0.
   - Rejected: a root-level workspace — the repo has no root JS workspace; adding one changes CI install paths for the frontend too.
3. **Keep type-check and lint.** The frontend covers the suite today only because `tsconfig` includes
   `**/*.ts` and ESLint runs over `tests/**`. After the move that coverage would vanish silently, so
   `testing/e2e` gets `tsconfig.json` and an ESLint flat config with the same three relaxed rules, run
   in CI right after `npm ci` and before the stack starts (fails fast, no new job, no new check name)
   and in `preflight.sh`.
4. **Remove Playwright from the frontend**, including the unused `playwright` runtime dependency.
   `npm install --package-lock-only` regenerates the lockfile; the diff must contain only Playwright
   packages, checked before commit.
5. **Port the reliability test to Python** (`scripts/test_e2e_reliability.py` plus a wrapper in
   `scripts/tests/`). It only reads files, so a Python guard is equivalent, runs without installing
   the frontend, and sits next to the other static guards. Each assertion is ported one to one; a
   mutation check proves it still fails (add `waitForTimeout(` to a hotspot spec, change the retries).
   - Rejected: running it with Playwright's runner — the global setup would require a live stack.
   - The wrapper is part of this change; the older unwrapped guards stay with #1209.
6. **CI keeps names.** `playwright-e2e.yml` changes working directories and paths only. Node cache uses both
   lockfiles. The frontend is still installed and built because the stack serves it. Artifact names stay
   `playwright-report`, `playwright-traces`; the paths inside them change.
7. **Fixtures path.** The two fixture files move to `testing/e2e/tests/fixtures/`, which stays git-ignored
   through `testing/e2e/.gitignore`.
8. **Constitution exemption.** The stale-path guard exempts `CONSTITUTION.md` with a comment naming #1210;
   #1210 deletes the exemption. The repository is inconsistent with the Constitution between the two merges,
   so #1210 should merge right after, or together if the Owner prefers.
9. **Sequence.** #1209 lands first so the guard this change extends (`test_testing_standalone.py`) is
   actually enforced; otherwise the new layout checks would be unenforced like the phase 1 ones.
10. **TDD.** Layout, frontend-free, self-containment and legacy-reference checks are added to
    `scripts/test_testing_standalone.py` and the reliability guard is ported first; they fail until the move.

## Riesgos / Trade-offs

- [A path constant is missed and the suite fails at runtime] → the same-results scenario compares against the 530/0/14 baseline from a full run, not only static checks.
- [`frontend/package-lock.json` churn] → regenerated lock-only; diff reviewed to Playwright packages only; frontend typecheck, lint, Vitest and build rerun.
- [The `Playwright E2E` required check breaks] → workflow, job and artifact names are asserted unchanged by the guard.
- [Developers' muscle memory: `npm run test:e2e` in `frontend/`] → removed deliberately; every doc and rule gives the new command.
- [CI cache misses on a second lockfile] → `cache-dependency-path` lists both.
- [Constitution contradicts the repo between merges] → #1210, flagged for same-day merge.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Suite and tooling live under testing/e2e; old locations gone; no spec lost | static | `scripts/test_testing_standalone.py` |
| Frontend is free of Playwright | static | same |
| e2e does not reference paths outside itself | static | same |
| Reliability guard collected and passes / fails on a violation | static + mutation | `scripts/test_e2e_reliability.py` via `scripts/tests/` wrapper |
| Type-check and lint pass | static | `npx tsc --noEmit`, `npx eslint .` in `testing/e2e` |
| CI and preflight run the moved suite; names unchanged | static | `scripts/test_testing_standalone.py` |
| The same suite passes after the move | E2E | `bash scripts/run_pipeline.sh` against baseline 530 / 0 / 14 |
| No live reference to the old location | static | `scripts/test_testing_standalone.py` |

- New unit tests (`src/test/java/.../unit/`): n/a (no Java touched)
- New integration tests: n/a
- Coverage impact: JaCoCo none; Vitest loses one test file that asserted nothing about `src/` (thresholds re-measured)

## Regression Strategy

- Existing tests affected: `e2e-test-reliability.test.ts` is replaced by its Python port; frontend Vitest, typecheck, lint and build rerun.
- Full suite command: `bash scripts/preflight.sh`, then `bash scripts/run_pipeline.sh`; `mvn verify -pl backend-api` only if Java is touched.
- HTTP/Bruno API suite: unchanged; run by the pipeline.
- Legacy paths at risk: none.

## Playwright Strategy

This change moves the suite itself. Evidence the move is faithful: the full run from `testing/e2e` reports 530 passed,
0 failed, 14 skipped, identical to the baseline; the 14 skips remain the documented feature-gap skips. All
projects (`smoke`, `health`, `chromium`, `mobile`) are exercised by CI.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR; `git mv` commits separate from content edits; the workflow change lands with the move so no job points at a missing path.
- Configuration or `.env` keys: `testing/.env.example` gains the E2E variables; none new in the application.
- Feature flag: no
- Smoke test after deploy (Gate 5): `playwright-e2e.yml` green on the merge commit, the `Playwright E2E` check present and green, and a local `cd testing/e2e && npx playwright test --project=smoke`.

## Rollback Strategy

- Revert the PR: the suite, workflow and frontend packaging return together. No data or runtime state is involved.
