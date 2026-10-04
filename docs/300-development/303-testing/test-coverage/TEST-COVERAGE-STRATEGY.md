# Test Coverage Strategy & Automation

Comprehensive test coverage measurement and automated reporting across all layers.

## Current Coverage Status

| Layer | Type | Coverage | Target |
|-------|------|----------|--------|
| Backend (Java) | JaCoCo Lines | ~84% | 80% |
| Backend (Java) | JaCoCo Branches | ~74% | 80% |
| Frontend (React) | Vitest | ~15% lines / ~10% branches (2026-10-03) | raise-only floor 14/9/10/14 (stmt/branch/fn/line); target 80% |
| E2E (UI) | Playwright | 33 spec files, per Caso de Uso | — |
| API | Bruno (YAML suite) | 104 requests, 16 resources | — |

Current figures per `.claude/rules/code-quality.md` (backend) and
[`backend-api/api-test/COVERAGE.md`](../../../../backend-api/api-test/COVERAGE.md) (API). Re-verify
before quoting exact numbers elsewhere — they drift.

## Automation

**GitHub Actions** (`.github/workflows/test-coverage-report.yml`):

- Runs on every push to `main`
- Runs on every PR
- Runs daily at 02:00 UTC
- Publishes reports to GitHub Pages

**Coverage Script** (`testing/scripts/generate-coverage-report.sh`):

```bash
bash testing/scripts/generate-coverage-report.sh
```

## Backend Testing (Spring Boot + JaCoCo)

### Running Tests

```bash
mvn test -pl backend-api
mvn jacoco:report -pl backend-api
open backend-api/target/site/jacoco/index.html
```

### Coverage Enforcement

- Ratchet floor: 70% line, 25% branch (enforced via `mvn verify`; raised as coverage improves, never lowered)
- Target: 80% line / 80% branch (aspirational)
- Excluded: legacy `jpa` and `service.Administrador*` packages

## Frontend Testing (React + Vitest)

### Running Tests

```bash
cd frontend
npm run test:coverage
npm test -- Button.test.tsx
open coverage/index.html
```

### Thresholds (raise-only, #976)

Enforced floor in `frontend/vitest.config.ts` (as of 2026-10-03): **14%**
statements / **9%** branches / **10%** functions / **14%** lines. Raise as
coverage improves; never lower without ADR/exception. Policy and root-cause
note for the historical <6% branch failure: `.claude/rules/code-quality.md`
and [`FRONTEND-TESTING-GUIDE.md`](../FRONTEND-TESTING-GUIDE.md). Guard test:
`frontend/src/tests/unit/vitest-coverage-thresholds.test.ts`.

## E2E Testing (Playwright)

### Running Tests

```bash
cd testing/e2e
npm test
npm run test:headed   # watch mode
```

### Coverage

- 33 spec files under `testing/e2e/tests/`, mostly one per Caso de Uso (`cuNN-*.spec.ts`)
- Full stack must be running (`bash scripts/start.sh`)

## API Testing (Bruno)

### Running Tests

```bash
cd backend-api/api-test
bru run . -r --env Development
```

### Coverage

See [`backend-api/api-test/COVERAGE.md`](../../../../backend-api/api-test/COVERAGE.md)
for the current request count and per-resource breakdown — re-run `bru run`
against a live backend for up-to-date pass/fail totals rather than quoting a
historical snapshot here.

## Reports

### GitHub Pages Dashboard

- **URL**: `https://matiaspakua.github.io/notaire/coverage/`
- **Updated**: Every commit + nightly
- **Shows**: Real-time metrics, trends, links to detailed reports

### PR Comments

Automatically adds coverage summary to PRs showing:

- Coverage deltas
- Links to full reports
- Trend indicators

## Tools Used

- **Backend**: JUnit 5, Mockito, JaCoCo, AssertJ, Spring Boot Test
- **Frontend**: Jest/Vitest, React Testing Library, c8
- **E2E**: Playwright
- **API**: Bruno CLI

---

See also: [CONSTITUTION.md](../../../../CONSTITUTION.md) — Mandatory test requirements
