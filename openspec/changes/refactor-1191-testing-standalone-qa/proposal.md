# Prepare testing/ as a standalone QA repository — phase 1

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1191 (phase 1 of umbrella #1190; phase 2 is #1192) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure (database suite also serves CU75) |
| Branch | `refactor/1191_testing_standalone_qa` |
| Gate 1 status | draft — awaiting Owner approval |

## Objetivo

Give a separate QA team one self-contained folder to verify and validate Notaire as
black-box modules, from integration to acceptance. Phase 1 builds the folder's structure,
replaces nine overlapping scripts with one runner, adds the missing black-box **database
suite** (initialisation, configuration, Flyway migration, seed data), moves the k6 load test
in as the performance suite, and documents it. Unit and API tests stay in the modules; the
Playwright move is phase 2 (#1192) because it needs a Constitution amendment.

## What Changes

- Layout `testing/{integration,database,performance,scripts,docs}` plus `README.md` and
  `.env.example`; `testing/e2e-swing/` is left exactly where it is (an existing spec allows it).
- One runner `testing/scripts/run.sh <suite>`; `scripts/test.sh` stays as a thin wrapper because
  Constitution step 14, preflight, CLAUDE.md and AGENTS.md call it.
- Remove what nothing references: `run-all-tests.sh`, `scripts/test-all.sh`,
  `scripts/run-comprehensive-tests.sh`, the root `generate-coverage-report.sh` (a non-identical
  duplicate of `scripts/generate-coverage-report.sh`, the one CI uses) and the committed
  `reports/` from 2026-04.
- New `testing/database/`: starts an empty PostgreSQL in Docker, applies the application's Flyway
  migrations with the Flyway CLI and checks the outcome with SQL only.
- Move `infra/performance/k6` to `testing/performance/k6` (it validates non-functional
  requirements, so it is V&V, not infrastructure); amend the infra spec accordingly.
- CI and local gates: new `database-vv.yml`, `preflight.sh --full` entry and `--list` row,
  `performance-test.yml` repointed, in the same PR.
- Docs: `testing/README.md` and `testing/docs/*`; `docs/` links to them.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| System-level V&V assets MUST live under `testing/`; module-level unit and API tests MUST stay in their module | #1190 (Owner decision); Constitution §7 | New |
| `testing/` MUST be self-contained; its seams to the application are documented and overridable | #1190; #1179 pattern | New |
| The database suite MUST use only SQL and the Flyway CLI against an empty PostgreSQL, with no Spring context | #1191 | New |
| Migrations stay in `backend-api` and are consumed read-only; the suite never edits them | Constitution P6 | Made explicit |
| Performance (k6) belongs to `testing/`, not `infra/` | #1191; amends #1179 | Changed |
| A gate added to CI MUST be added to `preflight.sh` in the same PR | CLAUDE.md CI Preflight | Made explicit |

## Capabilities

### New Capabilities

- `testing-standalone-repo`: layout, runner, database suite, self-containment and documentation
  contract for `testing/`.

### Modified Capabilities

- `infra-standalone-repo`: its first requirement no longer lists k6; load tests move to `testing/`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Migrations are only read; no Java change |
| `frontend` | no | Playwright stays until phase 2 |
| `frontend-swing` | no | Removed module |
| `notaire-shared` | no | — |
| `testing/` | yes | Reorganised, consolidated, new `database/` suite, new docs |
| `infra/` | yes | `performance/` removed; docs and layout updated |
| CI/CD (`.github/workflows`) | yes | New `database-vv.yml`; `performance-test.yml` path |
| Scripts / tests | yes | `preflight.sh`; new `test_testing_standalone.py`; repointed guards |

### Surface area

- Entities / Endpoints / Flyway: none changed (migrations are read-only inputs)
- Configuration / `.env`: new `testing/.env.example`
- Dependencies: Docker images `postgres:16.15` and `flyway/flyway:12.4.0` (pinned; Flyway matches the backend's, Spring Boot 4.1.1)

### Architecture review

Test-structure refactor with one new suite. No ADR: the same pattern as #1179 applied to QA.
Seams that remain and are documented: the running stack's base URL, the migrations directory,
and `generate-coverage-report.sh`, which reads `backend-api/target` and `frontend/coverage`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `testing/README.md` (rewritten) and `testing/docs/{PREPARATION,CONFIGURATION,DEFINITION,OPERATION}.md` (new) | Authoritative how-to for the QA suites |
| `docs/300-development/303-testing/README.md` and `TEST-PLAN.md` | Keep the strategy; link to `testing/` for running details; fix location table |
| `docs/300-development/CI-PREFLIGHT.md` | New `--full` gate, `database-vv.yml` mapping |
| `docs/300-development/303-testing/test-coverage/TEST-COVERAGE-STRATEGY.md` | Paths of the retained coverage script |
| `infra/README.md`, `infra/docs/DEFINITION.md` | k6 no longer in `infra/` |
| `CLAUDE.md`, `AGENTS.md` | Test commands |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` and `CU75 – Database Management and Migrations.md` | Add #1191 to the GitHub ID tables |
| `CHANGELOG.md` | `[Unreleased]` entry |

## Out of Scope

- Moving Playwright E2E and amending the Constitution (#1192).
- Creating the real repository and splitting history (phase 3).
- Moving Bruno or any backend/frontend unit test.
- Deleting `testing/e2e-swing/` (allowed to remain by `swing-e2e-retirement`).
- Changing Flyway migrations, ever.
