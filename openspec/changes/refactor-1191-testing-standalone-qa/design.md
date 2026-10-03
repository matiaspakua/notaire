> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1191, phase 1 of umbrella #1190, Use Case CU76 (database suite also CU75). Assessment of
`testing/` on `main` (2026-10-03):

| Finding | Detail |
|---------|--------|
| Size | 34 tracked files; `e2e-swing/` (Swing Robot, legacy) is 14 of them |
| Overlapping runners | `run-all-tests.sh` (742 lines), `scripts/test-all.sh`, `scripts/run-comprehensive-tests.sh`, `scripts/test.sh`, two non-identical coverage generators |
| References outside `testing/` | `scripts/test.sh` 22 (Constitution step 14, preflight, CLAUDE.md, AGENTS.md, agent rules); `scripts/generate-coverage-report.sh` 1 (`test-coverage-report.yml`); `http/test-all-endpoints-v2.sh` 4; **`run-all-tests.sh`, `test-all.sh`, `run-comprehensive-tests.sh`, `integration/e2e-login-and-stack.sh` and the root `generate-coverage-report.sh`: 0** |
| Reports | three generated reports from 2026-04-14 committed under `testing/reports/` |
| Database coverage | only white-box (Spring context): `FlywaySchemaValidationIntegrationTest`, six `*PgIntegrationTest`, `FlywayMigrationScriptTest`; nothing runs migrations against an empty server without Spring |
| Migrations | 41 versioned files plus `R14__restore_presupuestos_fk_id_tramite.sql`; Flyway 12.4.0 (Spring Boot 4.1.1); backend uses `baseline-on-migrate=true`, `baseline-version=0`, `clean-disabled=true` |
| Images in use | `postgres:16.15` (prod compose, CI); `flyway/flyway:12.4.0` exists |
| Playwright | in `frontend/`, zero imports from frontend source — phase 2 |

## Goals / Non-Goals

**Goals:**

- A self-contained `testing/` a QA team can run without reading the application code.
- One entry point; no dead scripts.
- A black-box database suite that proves init → configuration → migration → seed data.

**Non-Goals:**

- Playwright move and Constitution amendment (#1192); the real repository split; Bruno; any
  migration change.

## Decisions

1. **Layout by suite, not by tool**: `integration/` (cURL suite and stack smoke), `database/`,
   `performance/`, plus `scripts/`, `docs/`. `e2e-swing/` stays at its path because
   `swing-e2e-retirement` allows it and `test_dependabot_hygiene.py` checks its README.
   - Alternative rejected: move `e2e-swing` to `legacy/` — needs spec and guard changes for
     files nobody runs.
2. **One runner, `scripts/run.sh <suite>`** with `--list`; `test.sh` becomes a one-line wrapper
   so the 22 callers and Constitution step 14 keep working unchanged.
   - Alternative rejected: keep `run-all-tests.sh` as the aggregator — 742 lines, unreferenced,
     and it re-implements what preflight and the pipeline already compose.
3. **Delete only what has zero references**, with the evidence above. `e2e-login-and-stack.sh`
   is unreferenced but is a real test (DB + backend + login), so it is folded into the
   `integration` suite and wired to the runner, not deleted.
4. **Database suite = Docker Compose + Flyway CLI + SQL files**, no Spring, no Testcontainers.
   - `postgres:16.15` and `flyway/flyway:12.4.0`, the versions the product runs.
   - Migrations mounted read-only at `/flyway/sql` from `MIGRATIONS_DIR` (default
     `../backend-api/src/main/resources/db/migration`, marked `TESTING_APP_SEAM`); the suite
     never edits them (P6).
   - Same Flyway settings as the backend: `baselineOnMigrate=true`, `baselineVersion=0`,
     `cleanDisabled=true`.
   - No published host port, so it cannot collide with a running stack (lesson of #1186).
   - SQL assertion files, run in order, each printing PASS/FAIL lines; the runner exits non-zero
     on any FAIL. Table and role names are read from the migrated database, not assumed.
   - Includes a negative check: a *copy* of an applied migration is altered and `flyway validate`
     must fail, which proves checksum protection works.
   - Alternative rejected: pytest + psycopg — adds a toolchain; the existing `testing/` and
     preflight are shell-based.
5. **`R14__` file**: Flyway documents repeatable migrations as `R__name`. How the CLI treats
   `R14__…` versus the backend's classpath scan is unknown. If it differs, the suite records it as
   a finding in the PR; the migration is not changed.
6. **k6 moves to `testing/performance/k6`**: it verifies NFRs (CU74 SLOs), which is V&V. It makes
   #1179's placement wrong, so the infra spec is amended by a MODIFIED delta in this change.
7. **CI and preflight together**: new `database-vv.yml`, path-filtered on
   `backend-api/src/main/resources/db/migration/**` and `testing/database/**`, plus a
   `preflight.sh --full` entry and `--list` row.
8. **Retained app seam**: `scripts/generate-coverage-report.sh` reads `backend-api/target` and
   `frontend/coverage`; it stays (CI uses it) and is listed as a seam with `TESTING_APP_SEAM`.
9. **TDD**: `scripts/test_testing_standalone.py` encodes layout, deletions, self-containment and
   wiring and is written red first; the database checks are written before the compose harness
   so the first run fails.

## Riesgos / Trade-offs

- [Flyway CLI and the backend disagree on `R14__`] → recorded as a finding, not "fixed" (P6).
- [Docker image pulls make the suite slow or flaky offline] → pinned tags; CI caches; the suite
  is a separate job so it cannot block unrelated PRs.
- [Deleting scripts someone runs by hand] → zero references in code, docs and CI; recorded in the
  PR; revert is trivial.
- [Moving k6 breaks the weekly workflow] → path updated in `performance-test.yml` and its guard
  in the same PR.
- [Docs drift] → Gate 3 names every document.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Suites live under testing/; dead scripts gone | static | `scripts/test_testing_standalone.py` |
| Runner lists/rejects; test.sh delegates | static + run | same |
| Migrations apply; second run no-op; edit detected | database (Docker) | `testing/database/checks/*.sql` via `run.sh database` |
| Config, exporter role, seed data, schema objects | database (Docker) | same |
| Pinned images, no host port | static | `scripts/test_testing_standalone.py` |
| Self-contained; env example; docs set and links | static | same |
| Gates in sync | static | same |

- New unit tests (`src/test/java/.../unit/`): n/a (no Java touched)
- New integration tests: the database suite itself
- Coverage impact (JaCoCo): none

## Regression Strategy

- Existing tests affected: `test_performance_test_assets.py`, `test_image_pins_and_dependabot.py`,
  `test_infra_standalone.py` (k6 path) — repointed, assertions unchanged except the infra layout
  list, which this change amends deliberately.
- Full suite command: `bash scripts/preflight.sh`, then `bash scripts/run_pipeline.sh`.
- HTTP/Bruno suites: the cURL suite runs through `run.sh integration`; Bruno untouched.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface and the Playwright suite does not move in this phase. The required Playwright
CI job must still pass unchanged.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR; moves (`git mv`) separate from content edits; CI
  workflow and preflight land with the code they run.
- Configuration or `.env` keys: new `testing/.env.example` (`BASE_URL`, `MIGRATIONS_DIR`, k6 vars).
- Feature flag: no
- Smoke test after deploy (Gate 5): `bash testing/scripts/run.sh database` green on merged
  `main`, `testing/scripts/test.sh` green against a running stack, `database-vv.yml` green.

## Rollback Strategy

- Revert the PR: restores the old layout and k6 location. No data, schema or runtime state is
  involved; the database suite only uses throwaway containers.
