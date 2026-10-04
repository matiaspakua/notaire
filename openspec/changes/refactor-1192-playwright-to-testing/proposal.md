# Move the Playwright E2E suite to testing/e2e — phase 2

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1192 (phase 2 of umbrella #1190; amendment split to #1210; guard wiring #1209) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `refactor/1192_playwright_to_testing` |
| Gate 1 status | draft — awaiting Owner approval |

## Objetivo

The UI E2E suite is system-level acceptance testing and already black-box (zero imports from the
frontend source; it needs a base URL and `@playwright/test`). It belongs in `testing/`, owned by QA and
ready to split into the QA repository, instead of inside the `frontend/` module. This change moves it
with its own package, configuration, type-check and lint, and repoints every consumer, without
changing a single test.

## What Changes

- `git mv frontend/tests/e2e` → `testing/e2e/tests` (keeps every relative import valid) and
  `frontend/playwright.config.ts` → `testing/e2e/playwright.config.ts`, with `testDir`, `globalSetup`,
  `globalTeardown`, the reporter path and the two fixture paths rewritten.
- New `testing/e2e/{package.json,package-lock.json,tsconfig.json,eslint.config.mjs}` so the suite
  installs, type-checks and lints on its own. The frontend used to cover this incidentally.
- Remove Playwright from `frontend/`: the `playwright` and `@playwright/test` dependencies, the two
  `test:e2e*` scripts, the Vitest `tests/e2e/**` exclude, and the ignore-file entries.
- Port `frontend/src/tests/unit/e2e-test-reliability.test.ts` (static assertions on the E2E tree) to
  `scripts/test_e2e_reliability.py` with a discoverable wrapper, so it keeps running and keeps failing
  when a rule is broken.
- CI and local gates: `playwright-e2e.yml` installs and runs from `testing/e2e` (workflow, job and
  artifact names unchanged), `preflight.sh --full`, `run_pipeline.sh`, `generate_e2e_coverage_report.py`.
- Docs, agent rules, OpenSpec schema templates and the README point at `testing/e2e`.
- `CONSTITUTION.md` is **not** edited here; #1210 amends it and this change exempts it explicitly.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| System-level acceptance suites live under `testing/`; the frontend keeps only Vitest | #1190 Owner decision | New |
| The moved suite MUST run the same specs with the same results as before | #1192 | Made explicit |
| Moving the suite MUST NOT drop its type-check or lint coverage | Constitution P8 | Made explicit |
| Workflow, job and check names used by the `protect-main` ruleset MUST NOT change | #1040 ruleset | Made explicit |
| A Constitution amendment MUST be its own owner-reviewed PR | Constitution §12 | Respected (#1210) |

## Capabilities

### New Capabilities

- `e2e-suite-in-testing`: location, self-containment, static checks and wiring of the Playwright suite.

### Modified Capabilities

- (none under `openspec/specs/`; `testing-standalone-repo` listed Playwright as "not here" for phase 1, so
  its documentation row changes but no requirement does)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | Playwright dependencies, scripts and config removed; one unit test moved out |
| `frontend-swing` | no | Removed module |
| `notaire-shared` | no | — |
| `testing/` | yes | Gains `e2e/` |
| CI/CD (`.github/workflows`) | yes | `playwright-e2e.yml` paths and install |
| Scripts / tests | yes | `preflight.sh`, `run_pipeline.sh`, `generate_e2e_coverage_report.py`, new guard and wrapper |

### Surface area

- Entities / Endpoints / Flyway: none
- Configuration / `.env`: `BASE_URL` / `BACKEND_URL` semantics unchanged; documented in `testing/.env.example`
- Dependencies: `testing/e2e` gets its own lockfile; `frontend/package-lock.json` loses the Playwright packages

### Architecture review

Relocation plus packaging; no behaviour change. No ADR. The Constitution wording follows in #1210.
Remaining seams, all by environment variable: the running stack (`BASE_URL`, backend URL) and the
credentials the setup uses to log in.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `testing/README.md`, `testing/docs/*` | Add the `e2e` suite: prepare, configure, run, extend |
| `testing/.env.example` | `BASE_URL`, backend URL, E2E credentials and cleanup flag |
| `docs/300-development/303-testing/{README,TEST-PLAN,E2E-TEST-MAPPING,FRONTEND-TESTING-GUIDE}.md` and `test-coverage/TEST-COVERAGE-STRATEGY.md` | Paths and commands |
| `docs/200-architecture/202-ADR/ADR-005-modern-frontend-migration.md`, `203-design/FRONTEND-WORKFLOW-TRACKER.md` | Path references |
| `README.md`, `AGENTS.md`, `CLAUDE.md`, `.claude/rules/*`, `.claude/agents/*`, `.codex/agents/*`, `.claude/skills/*`, `.agents/skills/*`, `local-ai/sdlc/WORKER.md` | Command and path |
| `openspec/schemas/notaire-sdlc/{schema.yaml,templates/design.md,templates/tasks.md}`, `openspec/NOTAIRE-ADAPTATIONS.md` | Command and path used by every future change |
| `docs/300-development/CI-PREFLIGHT.md` | `--full` runs Playwright from `testing/e2e` |
| `CHANGELOG.md` | `[Unreleased]` entry |
| `CONSTITUTION.md` | Not here: #1210 |

## Out of Scope

- Changing, adding or skipping any test; a test that fails after the move is a finding, not something to edit away.
- The `CONSTITUTION.md` amendment (#1210).
- Wiring the older unwrapped guards into CI (#1209), except the one this change adds.
- Creating the real QA repository (phase 3).
