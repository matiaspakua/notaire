# e2e-suite-in-testing Specification

## Purpose

Make the Playwright E2E suite a self-contained part of `testing/`. Source: #1192 (umbrella #1190); owner CU76.

## Requirements

### Requirement: The suite lives in testing/e2e

The specs, setup, reporters, Playwright configuration, `package.json`, lockfile, `tsconfig.json` and
ESLint configuration MUST live under `testing/e2e`. `frontend/tests/e2e` and
`frontend/playwright.config.ts` MUST NOT exist.

#### Scenario: The suite and its tooling live under testing/e2e

- **WHEN** the repository tree is inspected
- **THEN** `testing/e2e/tests`, `testing/e2e/playwright.config.ts`, `testing/e2e/package.json`,
  `testing/e2e/package-lock.json`, `testing/e2e/tsconfig.json` and `testing/e2e/eslint.config.mjs` exist

#### Scenario: The old locations are gone

- **WHEN** the repository tree is inspected
- **THEN** `frontend/tests` and `frontend/playwright.config.ts` do not exist

#### Scenario: No spec was lost

- **WHEN** the spec files under `testing/e2e/tests` are counted
- **THEN** the count equals the number that existed under `frontend/tests/e2e` before the move (52; 51 before #1208 added `workflow-tracker.spec.ts`)

### Requirement: The frontend no longer carries Playwright

`frontend/package.json` MUST NOT declare `playwright`, `@playwright/test` or a `test:e2e` script, and the
Vitest, ESLint and ignore configuration of the frontend MUST NOT reference the E2E tree.

#### Scenario: Frontend is free of Playwright

- **WHEN** `frontend/package.json`, `frontend/vitest.config.ts`, `frontend/eslint.config.mjs`,
  `frontend/.gitignore` and `frontend/.dockerignore` are read
- **THEN** none mentions Playwright or `tests/e2e`, and the lockfile has no `@playwright/test` entry

### Requirement: The suite is self-contained

Files under `testing/e2e` (excluding `node_modules`) MUST NOT reference a path outside `testing/e2e`.
The only couplings to the application are the environment variables for the running stack.

#### Scenario: e2e does not reference paths outside itself

- **WHEN** the source and configuration under `testing/e2e` are scanned
- **THEN** none resolves a path above `testing/e2e`, and the configuration, setup and reporter paths all
  resolve inside it

### Requirement: Static reliability rules are preserved and enforced

The assertions of the former `e2e-test-reliability.test.ts` MUST exist as a Python guard that CI and
preflight discover, and it MUST fail when a rule is broken.

#### Scenario: The reliability guard runs from the discovered test directory

- **WHEN** `python3 -m unittest discover -s scripts/tests` runs
- **THEN** the reliability guard is collected and passes

#### Scenario: The reliability guard fails on a violation

- **WHEN** a hotspot spec gains a `waitForTimeout(` call
- **THEN** the guard fails

### Requirement: Type-check and lint are preserved

The E2E tree MUST be type-checked with `tsc --noEmit` and linted with ESLint from `testing/e2e`, in CI before
the stack starts and in `preflight.sh`.

#### Scenario: Type-check and lint pass

- **WHEN** `npx tsc --noEmit` and `npx eslint .` run in `testing/e2e`
- **THEN** both exit 0

### Requirement: Gates run the moved suite and keep their names

`playwright-e2e.yml`, `preflight.sh --full` and `run_pipeline.sh` MUST run and report the suite from
`testing/e2e`. The workflow name, the job names `UI E2E Tests (Playwright)` and
`Playwright E2E`, and the artifact names MUST NOT change.

#### Scenario: CI and preflight run the moved suite

- **WHEN** the workflow and scripts are inspected
- **THEN** every Playwright step uses `testing/e2e` as its working directory or path, and no step
  references `frontend/test-results` or `frontend/playwright-report`

#### Scenario: The same suite passes after the move

- **WHEN** `bash workspace/sdlc/run_pipeline.sh` runs with the stack up
- **THEN** the Playwright run reports the same number of passed tests as the baseline recorded before the move

### Requirement: No active file points at the old location

No active file MUST reference the former Playwright location, so documentation, rules and tooling cannot send
anyone to a path that no longer exists.

#### Scenario: No live reference to frontend/tests/e2e

- **WHEN** tracked files outside the archives, `CHANGELOG.md` and `CONSTITUTION.md` are searched for
  `frontend/tests/e2e`, `frontend/playwright` and `cd frontend && npx playwright`
- **THEN** none is found, and `CONSTITUTION.md` is exempt only until #1210 lands
