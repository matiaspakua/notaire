# Backend logs go to the logging platform only

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1286 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `chore/1286_logs_to_loki_only` |
| Gate 1 status | draft |

## Objetivo

The backend writes a rolling JSON log file (`backend-api/logs/notaire-backend.log`, 33 MB when found) next to the source tree. Logs belong in the logging platform (`infra/`: Promtail, Loki, Grafana), not in the codebase. The JSON console appender already feeds Loki through Promtail's Docker discovery, so the file output is redundant.

## What Changes

- Remove `logging.file.name`, `logging.file.max-size` and `logging.file.max-history` from `application.properties` (they are what places the file under `backend-api/logs/` when the backend is started from that directory).
- Remove the `FILE` and `ASYNC_FILE` appenders and the `LOG_FILE` property from `logback-spring.xml`; the root logger keeps only the JSON `CONSOLE` appender.
- Git-ignore `backend-api/logs/` and delete the existing directory.
- A guard test asserts the logging configuration declares no file appender.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Application logs are shipped to Loki via structured JSON on stdout; the application never persists log files | CU77, `infra/README.md` | Made explicit |

## Capabilities

### New Capabilities

- `backend-logging`: logs leave the process only through the JSON console.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | logging configuration only; no Java production code |
| `frontend` | no | — |
| Docs / scripts / CI | yes | observability docs, CHANGELOG, `.gitignore` |

### Surface area

- Endpoints, entities, Flyway, dependencies: none
- Configuration: `logging.file.*` keys removed
- Risk: a backend run directly on the host (`mvn spring-boot:run`) is no longer visible in Loki and logs only to the terminal; Promtail discovers Docker containers only. Running the stack through `bash scripts/start.sh` is the supported path for centralized logs.

### Architecture review

No architectural change; aligns the backend with the existing Promtail/Loki pipeline.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `infra/README.md` | state that logs are stdout-only and where to query them |
| `CHANGELOG.md` | one entry |
