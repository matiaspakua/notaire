# Testing — Notaire

The project's testing framework: unit, integration, API, frontend, UI E2E
(Playwright), and traceability by use case.

For the master test plan (test levels, catalog by use case, and the reporting
process), see [`TEST-PLAN.md`](TEST-PLAN.md). This README is the inventory of
suites and commands.

```text
Unit → Integration → API (Bruno) → Frontend (Vitest) → E2E UI/UX (Playwright, by use case)
```

## Test suites

| Suite | Location | Volume | Command |
|-------|-----------|---------|---------|
| Unit (JUnit 5) | `backend-api/src/test/java/.../unit/` | 73 classes | `mvn test -pl backend-api -Dtest="**/unit/*"` |
| Integration (Spring Boot / H2 + PostgreSQL) | `backend-api/src/test/java/.../integration/` | 59 classes | `mvn test -pl backend-api -Dtest="**/integration/*"` |
| API (Bruno YAML) | `backend-api/api-test/` | 164 requests, 21 resource folders (idempotent) | `cd backend-api/api-test && bru run . -r --env Development` |
| Frontend unit/component (Vitest) | `frontend/src/**/*.test.ts(x)` | 19+ files | `cd frontend && npm test` |
| E2E UI/UX (Playwright) | `testing/e2e/tests/` | 33 specs, by use case (`cuNN-*.spec.ts`) | `cd testing/e2e && npm test` |
| Integration (cURL suite + stack smoke) | `testing/integration/` | 1 strict suite + 1 smoke | `bash testing/scripts/run.sh integration` |
| Database V&V (empty DB → Flyway → SQL checks) | `testing/database/` | 30 checks | `bash testing/scripts/run.sh database` |
| E2E Swing (Robot) | `testing/e2e-swing/` | **RETIRED** (#811 / ADR-012) | Do not run; see suite README |

Backend: 132 test classes, about 1,483 `@Test` methods combined (unit + integration).

System-level suites (integration and database) live in [`testing/`](../../../testing/README.md),
a self-contained folder intended to move to its own QA repository. The run,
configuration, and extension guide is there. Unit and API tests stay in
`backend-api/` and `frontend/` because they are part of each module's
development cycle.

## Detailed references

| Document | Contents |
|-----------|-----------|
| [`CU-API-MATRIX.csv`](CU-API-MATRIX.csv) | Traceability from use case to module, entity/operation, controller/endpoint, Bruno test, and issue. Drift guard: `python3 workspace/sdlc/validate-cu-api-matrix.py` (#1064) |
| [`FRONTEND-TESTING-GUIDE.md`](FRONTEND-TESTING-GUIDE.md) | Vitest conventions and the Playwright E2E spec layout |
| [`api-test/README.md`](api-test/README.md) | Manual HTTP/curl checks and API testing patterns |
| [`test-coverage/TEST-COVERAGE-STRATEGY.md`](test-coverage/TEST-COVERAGE-STRATEGY.md) | Coverage strategy by layer and report automation |
| [`backend-api/api-test/COVERAGE.md`](../../../backend-api/api-test/COVERAGE.md) | Current Bruno suite status (pass/fail, defects found) |

## Quick start

```bash
# Backend: unit + integration
mvn test -pl backend-api

# Backend: unit only (fast, about 10s)
mvn test -pl backend-api -Dtest="**/unit/*"

# Backend coverage
mvn jacoco:report -pl backend-api && open backend-api/target/site/jacoco/index.html

# Frontend: unit + coverage
cd frontend && npm run test:coverage

# Playwright E2E (needs the full stack: bash workspace/stack/start.sh)
cd testing/e2e && npm test
npm run test:headed   # interactive mode

# API (Bruno, needs the backend on :8080)
cd backend-api/api-test && bru run . -r --env Development

# Local copy of every CI gate
bash workspace/sdlc/preflight.sh --full
```

## Coverage

The required floor (ratchet) and the long-term target are in
[`.claude/rules/code-quality.md`](../../../.claude/rules/code-quality.md):
70% line / 25% branch (floor, enforced by `mvn verify`), 80%/80% (target).
Current: about 84% line / about 74% branch (backend, with `jpa` and
`service.Administrador*` excluded).

## CI/CD

Full map of local checks to CI jobs:
[`CI-PREFLIGHT.md`](../CI-PREFLIGHT.md). In short:

- **Pull request**: unit + integration (H2 + PostgreSQL), Checkstyle, Spotless (job "Code Lint"), SpotBugs, Playwright E2E
- **Merge to `main`**: full suite + coverage (ratchet floor) + Trivy
- **Release**: Docker image build + image scan

## Reports

```bash
open backend-api/target/site/jacoco/index.html   # backend coverage (JaCoCo)
open frontend/coverage/index.html                 # frontend coverage (Vitest)
open testing/e2e/playwright-report/index.html         # Playwright E2E report
```

The aggregated dashboard (GitHub Pages, updated by CI) is in
[`.github/workflows/test-coverage-report.yml`](../../../.github/workflows/test-coverage-report.yml).

## Swing client and Robot E2E (retired)

The Swing modules (`frontend-swing` / `deprecated-frontend-swing`) were removed
from the repository (#1046). `.github/workflows/e2e-swing.yml` is retired
(ADR-012 / #1083 / #811). Assets under `testing/e2e-swing/` are hard-deprecated
in place — do **not** wire them into CI or rebuild Swing. Active UI E2E is
Playwright (`testing/e2e/tests/`).

## Navigation

- [← Development](../)
- [Test Plan](TEST-PLAN.md)
- [Architecture](../../200-architecture/)
