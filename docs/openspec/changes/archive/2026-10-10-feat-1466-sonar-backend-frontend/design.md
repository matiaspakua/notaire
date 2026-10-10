> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6, §7, §11.

## Context

See [proposal.md](proposal.md). SonarQube Community 26.9 runs in the infra compose stack.
The existing script analyzes only the backend and has no gate.

## Goals / Non-Goals

**Goals:** analyze both modules, import coverage and lint, enforce a quality gate, one command.
**Non-Goals:** CI integration, PR decoration, branch analysis, changing login behaviour (#1413).

## Decisions

1. **Backend settings in `pom.xml` properties.** The Maven scanner reads them natively; the script
   stays small. Rejected: a root `sonar-project.properties` (the Maven scanner would ignore module paths).
2. **Frontend through the pinned `sonar-scanner-cli` container.** Avoids a Node scanner dependency in
   `frontend/package.json`. Rejected: `@sonar/scan` npm package (adds a runtime-adjacent dependency).
3. **ESLint JSON imported as external issues** (`sonar.eslint.reportPaths`), so lint findings appear
   next to Sonar's own rules.
4. **Quality gate via Web API from the script** (create/copy gate, set conditions, assign to both
   projects), idempotent. Rejected: manual UI setup (not versioned).
5. **Gate conditions are on new code** (the Sonar-recommended model for a legacy codebase): no new bugs or
   vulnerabilities, reliability/security rating A, new-code coverage >= 70%, duplication <= 5%.

## Riesgos / Trade-offs

- [First run reports many legacy issues] -> the gate evaluates new code only.
- [Scanner container needs network access to `localhost:9000`] -> use `--network host` on Linux and
  `host.docker.internal` on macOS, chosen by the script.

## Testing Strategy

| Scenario (spec) | Test level | Test |
|-----------------|------------|------|
| Backend analysis is submitted | configuration | `infra/tests/test_sonar_analysis.py::test_backend_properties` |
| Frontend analysis is submitted | configuration | `infra/tests/test_sonar_analysis.py::test_frontend_properties_and_reports` |
| Gate passes / fails | configuration | `infra/tests/test_sonar_analysis.py::test_script_enforces_quality_gate` |
| No secret in configuration | configuration | `infra/tests/test_sonar_analysis.py::test_no_credentials_in_configuration` |

Plus a live run against the local SonarQube.

- New unit/integration tests (Java): None. Coverage impact: none.

## Regression Strategy

- `bash infra/verify.sh` and existing `infra/tests/test_*.py` stay green.
- Full suite: `mvn verify -pl backend-api`.

## Playwright Strategy

n/a - no product UI change.

## Deployment Strategy

- Flyway migration: no. `.env` keys: none new. Feature flag: no.
- Smoke test (Gate 5): run the script; both projects appear in SonarQube with a gate status.

## Rollback Strategy

- Revert safe: yes (config and script only). No data migration; Sonar projects can be deleted.

## Migration Plan

1. Write failing guards. 2. Add backend/ frontend properties. 3. Extend the script. 4. Docs.
5. Live run, then PR.
