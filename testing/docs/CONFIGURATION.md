# Configuring the QA suites

Everything configurable, and where. Template: [`../.env.example`](../.env.example). Lookup order
used by `scripts/run.sh`: `$TESTING_ENV_FILE`, then `testing/.env`.

## Variables

| Variable | Used by | Default | Meaning |
|----------|---------|---------|---------|
| `E2E_BASE_URL` | `e2e` | `http://localhost:3000` | Frontend the Playwright suite drives; `run.sh` passes it to Playwright as `BASE_URL` |
| `BASE_URL` | `integration` | `http://localhost:8080` | API under test; honoured by every cURL script |
| `MIGRATIONS_DIR` | `database` | `../backend-api/src/main/resources/db/migration` | Flyway migrations to apply; relative paths resolve from `testing/` |
| `POSTGRES_EXPORTER_USER` | `database` | `notaire_exporter` | Flyway placeholder `exporterUsername` for migration V12 |
| `POSTGRES_EXPORTER_PASSWORD` | `database` | `vv-exporter-throwaway` | Flyway placeholder `exporterPassword` for migration V12 |
| `DB_VV_PASSWORD` | `database` | `vv-throwaway` | Password of the throwaway PostgreSQL |

Optional overrides read directly from the environment:

| Variable | Default | Meaning |
|----------|---------|---------|
| `TESTING_ENV_FILE` | `testing/.env` | Explicit env file |
| `EXPECTED_IGNORED` | `R14__restore_presupuestos_fk_id_tramite.sql` | Space-separated SQL files Flyway must ignore on purpose |
| `DB_VV_PROJECT` | `notaire-db-vv-<pid>` | Docker Compose project name; unique per run by default |

## Parity with the backend

The database suite runs Flyway the way the backend does, so a green result means something:

| Setting | Backend (`application.properties`) | Suite |
|---------|------------------------------------|-------|
| Flyway version | 12.4.0 (Spring Boot 4.1.1) | `flyway/flyway:12.4.0` |
| PostgreSQL | 16.15 (prod compose, CI) | `postgres:16.15` |
| `baselineOnMigrate` / `baselineVersion` | `true` / `0` | same |
| `cleanDisabled` | `true` | same |
| Placeholders | `exporterUsername`, `exporterPassword` | same two, from the variables above |

When a new migration introduces another placeholder, the suite fails on the missing value, as
Flyway does. Add the variable to `.env.example`, to `database/run.sh` and to this table.

## Pinned images

Both images in `database/docker-compose.yml` are pinned to an exact version. Bump them together
with the product, deliberately, in a pull request; `scripts/test_image_pins_and_dependabot.py`
rejects `:latest` and major-only tags.
