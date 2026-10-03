# Notaire QA — verification and validation

Everything a QA team needs to verify and validate Notaire as **black-box modules**, from
integration to acceptance. This folder is self-contained and prepared to live in its own
repository; for now it sits inside the application repository (#1190, #1191).

Unit tests and API tests stay with `backend-api/` and `frontend/` because they are part of each
module's development loop. The project-level testing strategy stays in
[`docs/300-development/303-testing/`](../docs/300-development/303-testing/README.md); everything
specific to running and extending these suites is documented here.

## Guides

| Guide | Answers |
|-------|---------|
| [PREPARATION](docs/PREPARATION.md) | What do I need installed and running first? |
| [CONFIGURATION](docs/CONFIGURATION.md) | Which variables and overrides do I set? |
| [DEFINITION](docs/DEFINITION.md) | What does each suite verify, and where does it touch the application? |
| [OPERATION](docs/OPERATION.md) | How do I run, read, extend and troubleshoot the suites? |

## Suites

| Suite | What it verifies | Needs | Command |
|-------|------------------|-------|---------|
| `integration` | The running stack through its HTTP surface (cURL suite and a stack smoke) | A running stack | `bash testing/scripts/run.sh integration` |
| `database` | An **empty** PostgreSQL migrated by Flyway: history, configuration, roles, seed data, schema, idempotence, tamper detection | Docker only | `bash testing/scripts/run.sh database` |

`bash testing/scripts/run.sh --list` prints the suites. `testing/scripts/test.sh` is kept as the
stable entry point for the integration suite (Constitution step 14 and the agent rules call it).

## Layout

```text
testing/
  scripts/       run.sh (runner), test.sh (stable wrapper), generate-coverage-report.sh
  integration/   http/ (cURL suite), e2e-login-and-stack.sh (stack smoke)
  database/      docker-compose.yml, run.sh, checks/*.sql
  docs/          the four guides above
  e2e-swing/     retired Swing Robot suite, kept on disk (see DEFINITION)
  .env.example   variables these suites use
```

## Quick start

```bash
cp testing/.env.example testing/.env     # once
bash testing/scripts/run.sh database      # needs Docker; about 10 seconds
bash scripts/start.sh                     # start the application, then:
bash testing/scripts/run.sh integration
```

## Not here, on purpose

| Asset | Where | Why |
|-------|-------|-----|
| Unit, Spring and Testcontainers tests, Vitest | `backend-api/`, `frontend/` | Module development loop |
| Bruno API collection | `backend-api/api-test/` | API tests belong to the backend |
| k6 load test | `infra/performance/k6` | Infrastructure assets belong to the infra repository |
| Playwright E2E (UI) | `frontend/tests/e2e` for now | Moves here in phase 2 (#1192), which also amends the Constitution |
