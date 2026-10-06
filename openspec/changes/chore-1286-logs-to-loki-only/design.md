> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1286, Use Case CU77. `logging.file.name=logs/notaire-backend.log` is resolved against the working directory, which creates `backend-api/logs/` for host runs; Logback's `FILE` appender additionally writes `${java.io.tmpdir}/spring.log`. Promtail (`infra/observability/loki/promtail-config.yaml`) collects Docker container stdout and parses the `LogstashEncoder` JSON envelope.

## Goals / Non-Goals

**Goals:** no log file written by the backend; Loki remains the single place to read logs.
**Non-Goals:** adding a direct Loki appender for host runs (new dependency, not needed for the supported Docker flow); changing log levels or the JSON schema.

## Decisions

1. Remove file output instead of redirecting it: stdout is already collected, and a second sink only duplicates data. Rejected: moving the file to `/tmp` (still a local file, no consumer), adding `loki-logback-appender` (extra dependency and a second path to Loki for a non-supported run mode).
2. Guard with a unit test that parses `logback-spring.xml` and `application.properties`, because a regression here is silent (files just reappear).

## Riesgos / Trade-offs

- Host runs have no centralized logs; documented in `infra/README.md`.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| No file appender is configured | unit | `LoggingConfigurationTest` |
| No log file location is configured | unit | `LoggingConfigurationTest` |
| Log directory is not tracked | manual | `git check-ignore backend-api/logs` |

- Coverage impact: none (configuration only)

## Regression Strategy

- Full suite command: `mvn verify -pl backend-api`; `bash scripts/preflight.sh`
- Docker smoke: `{container_name="notary-backend"}` in Loki still returns JSON lines

## Playwright Strategy

No UI change; Playwright not applicable.

## Deployment Strategy

- Flyway migration required: no; configuration keys removed: `logging.file.*`
- Smoke test after deploy (Gate 5): backend logs visible in Grafana `notaire-logs`

## Rollback Strategy

- Revert the PR.
