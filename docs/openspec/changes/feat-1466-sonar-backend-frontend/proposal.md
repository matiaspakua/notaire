# Analyze backend and frontend with SonarQube Community

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1466 |
| Use Case | CU78 – Security and Compliance |
| Branch | `feat/1466_sonar_backend_frontend` |
| Gate 1 status | Pending |

## Objetivo

SonarQube runs locally but analyzes only the backend through an ad-hoc script, with no
quality gate, no frontend project and no lint report. Make one command analyze both
modules with coverage, test results and lint findings, and fail when the quality gate fails.

## What Changes

- Backend project settings (`sonar.*`) move into `backend-api/pom.xml` properties: sources, exclusions
  (legacy `jpa` package, as in the JaCoCo gate), JaCoCo XML path, test reports.
- New frontend project `notaire-frontend` configured by `frontend/sonar-project.properties`: Vitest `lcov`,
  ESLint JSON report, TypeScript sources and exclusions.
- `infra/scripts/run-sonar.sh` analyzes both projects, applies a "Notaire way" quality gate and the
  new-code period through the Sonar Web API, waits for the gate, and exits non-zero on failure.
- The frontend is analyzed with the pinned `sonarsource/sonar-scanner-cli` container, so no Node scanner
  dependency is added to the application.
- Guard tests, documentation of every Community feature used (and which are paid-edition only).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Code quality and security findings must be measured continuously on both modules. | `CU78 – Security and Compliance`; `.claude/rules/code-quality.md` | Made explicit |
| A failing quality gate must be visible and must fail the analysis command. | CU78 | New |
| Coverage in Sonar must equal the project's own gates (JaCoCo, Vitest). | `.claude/rules/code-quality.md` | Made explicit |

## Capabilities

### New Capabilities
- `sonar-code-analysis`: SonarQube analysis of backend and frontend with a quality gate.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | Yes (build config only) | `sonar.*` properties in `pom.xml`; no Java change. |
| `frontend` | Yes (config only) | `sonar-project.properties`; no source change. |
| `frontend-swing` | No | Retired. |
| `infra` | Yes | `run-sonar.sh`, guard tests, docs. |
| CI/CD | No | Local analysis only; CI integration is out of scope. |

### Surface area

- Entities / endpoints / Flyway: None.
- Configuration / `.env`: reuses `SONAR_*`; no new secret keys.
- Dependencies: pinned `sonarsource/sonar-scanner-cli` image used on demand.
- Compatibility: none broken; existing `run-sonar.sh` invocation keeps working.

### Architecture review

Follows ADR-016 (SonarQube inside the infra stack). No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `infra/docs/OPERATION.md` | Document the one-command analysis and its outputs. |
| `infra/docs/DEFINITION.md` | Add the Sonar projects and the quality gate. |
| `.claude/rules/code-quality.md` | Add SonarQube to the tools table. |
| `CHANGELOG.md` | N/A: local tooling. |

## Out of Scope

- Running Sonar in GitHub Actions; PR decoration and branch analysis (paid editions); changing the
  anonymous-access/login behaviour (#1413).
