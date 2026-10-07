# Preparing the QA environment

What you need before running anything in `testing/`. Next:
[CONFIGURATION](CONFIGURATION.md) → [DEFINITION](DEFINITION.md) → [OPERATION](OPERATION.md).

## Prerequisites

| Need | Why | Required for |
|------|-----|--------------|
| Docker with Compose v2 | runs the throwaway PostgreSQL and the Flyway CLI | `database` |
| `bash` 3.2 or newer, `curl` | the runner and the cURL suite (macOS and Linux both work) | both |
| A running Notaire stack | the system under test | `integration`, `e2e` |
| Node 22 and npm | installs and runs the Playwright suite (`npm ci` runs on first use) | `e2e` |
| Playwright browsers (`npx playwright install --with-deps chromium chrome webkit`) | the projects use Chrome, bundled Chromium and WebKit | `e2e` |
| Network access to pull `postgres:16.15` and `flyway/flyway:12.4.0` once | image pulls | `database` |

Python and Maven are **not** needed for `integration` and `database`; Node is needed only for `e2e`. The coverage script is the exception
(see DEFINITION): it builds the application and so needs Maven and Java 21.

## 1. The system under test (integration and e2e)

The integration and e2e suites talk to a stack that is already running. From the application repository:

```bash
bash workspace/stack/start.sh        # PostgreSQL, backend :8080, frontend :3000
```

Any other environment works by setting `BASE_URL` (see CONFIGURATION). The runner checks
`/actuator/health` (or the Swagger page) first and stops with a clear message if the API is down.

## 2. The migrations (database only)

The database suite applies the application's own Flyway migrations, mounted read-only. Inside the
application repository the default is `backend-api/src/main/resources/db/migration`. After a
repository split, point `MIGRATIONS_DIR` at a checkout of the application or at an exported copy.
The suite never edits a migration.

## 3. Environment file

```bash
cp testing/.env.example testing/.env
```

`testing/.env` is git-ignored. Nothing in it is a real credential; the values are local
placeholders for throwaway containers.

## 4. No ports to free

The database suite publishes **no host port** and keeps its data in memory, so it can run beside
a running stack and beside other runs. Each run uses its own Docker Compose project name.
