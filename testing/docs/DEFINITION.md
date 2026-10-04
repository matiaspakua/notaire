# What the QA suites define

What each suite verifies and where it touches the application. The project-level test strategy
(pyramid, coverage floors, Use Case traceability) stays in
[`docs/300-development/303-testing/TEST-PLAN.md`](../../docs/300-development/303-testing/TEST-PLAN.md).

## Ownership boundaries

| Level | Where | Owner |
|-------|-------|-------|
| Unit, Spring/H2 integration, Testcontainers `pg-integration` | `backend-api/src/test` | Backend developers |
| Vitest component tests | `frontend/src` | Frontend developers |
| Bruno API collection | `backend-api/api-test` | Backend developers (API tests belong to the module) |
| **Integration (HTTP surface, stack smoke)** | `testing/integration` | QA |
| **Database V&V** | `testing/database` | QA |
| **E2E (Playwright UI)** | `testing/e2e` | QA |
| k6 load test | `infra/performance/k6` | Infra |

## Integration suite

`run.sh integration` checks that the API answers, runs `integration/http/test-all-endpoints-v2.sh`
(strict-mode cURL assertions over the main resources) and then
`integration/e2e-login-and-stack.sh` (backend health, login, then authorization: an anonymous call is rejected with 401 and the login token is accepted with 200). The stack smoke had never run before this suite wired it in; it failed on macOS because of a GNU-only `head -n -1`, and its last step could not fail.

## E2E suite

Playwright drives the UI in a browser against a running stack. It is black-box: no spec imports
anything from `frontend/`; the only inputs are the frontend URL (`BASE_URL`, mapped from
`E2E_BASE_URL` by `run.sh`) and the backend the frontend talks to. Specs are named `TS-nnnn-*.spec.ts`
and mapped to Use Cases in
[`E2E-TEST-MAPPING.md`](../../docs/300-development/303-testing/E2E-TEST-MAPPING.md).

| Item | Where |
|------|-------|
| Specs, helpers, global setup, business-coverage reporter | `e2e/tests/` |
| Projects `smoke`, `health`, `chromium` (Chrome channel), `mobile` (iPhone SE, WebKit) | `e2e/playwright.config.ts` |
| Dependencies, type-check (`npm run typecheck`), lint (`npm run lint`) | `e2e/package.json`, `tsconfig.json`, `eslint.config.mjs` |
| Reliability rules (retry budget, sleeps, skip inventory) | `scripts/test_e2e_reliability.py` in the application repository |

Fixtures written at run time (`tests/fixtures/`) and the reports (`playwright-report/`, `test-results/`) are git-ignored.

## Database suite

Starts an **empty** PostgreSQL 16.15, applies the application's migrations with the Flyway CLI and
verifies the outcome with SQL. No Spring context is involved.

| Check | Verifies |
|-------|----------|
| migrate, then validate | Every migration applies to an empty database and the history validates |
| files versus history | Every `V<n>__*.sql` file is applied, and the only ignored SQL file is the documented manual rollback `R14__restore_presupuestos_fk_id_tramite.sql` |
| `10_history.sql` | No failed row; versions contiguous from 1 to the latest |
| `20_config.sql` | Server major version 16; database encoding UTF8 |
| `30_roles.sql` | The V12 exporter role exists, can log in, is not a superuser and belongs to `pg_monitor` only |
| `40_seed.sql` | Rows inserted by V2 and V10: users (default administrator), people, identification and document types, folio types, management statuses, concepts, procedure types, the demo workflow |
| `50_schema.sql` | Every table has a primary key; foreign keys exist and are all validated |
| renamed tables | Every table renamed by an `ALTER TABLE … RENAME TO` is gone under its old name (derived from the migrations themselves; `folios` keeps its name on purpose) |
| second migrate | A repeat `migrate` applies nothing and leaves the history unchanged |
| tamper detection | A copy of an applied migration with one extra line is rejected by `flyway validate` with a checksum mismatch, proving applied migrations are protected |

### Why `R14__…` is ignored

It is a deliberate manual rollback for V14 (its own header says Flyway must ignore it). The Flyway
CLI reports "1 SQL migrations were detected but not run because they did not follow the filename
convention", exactly as the backend's engine does. The suite asserts this so a stray misnamed
migration cannot hide behind it.

## Coupling with the application

`testing/` is self-contained except for these seams, which a repository split must replace:

| Seam | Today | After a split |
|------|-------|---------------|
| Running stack | `BASE_URL`, default `http://localhost:8080` | unchanged; point it at any environment |
| Migrations | `MIGRATIONS_DIR` defaults to `backend-api/src/main/resources/db/migration` | export the migrations (artifact or checkout) and set the variable |
| Flyway placeholders | V12 needs `exporterUsername` and `exporterPassword` | keep in sync with the backend configuration |
| Coverage script | `scripts/generate-coverage-report.sh` runs Maven in `backend-api` and reads `frontend/coverage`; used by `test-coverage-report.yml` | stays with the application repository, not part of QA V&V |
| CI | `.github/workflows/database-vv.yml` filters on the migrations path | move the workflow with the folder and change the trigger to the exported artifact |

## Legacy

| Asset | Status |
|-------|--------|
| `e2e-swing/` | Retired Swing Robot suite, kept on disk by `openspec/specs/swing-e2e-retirement`; never wire it into CI |
