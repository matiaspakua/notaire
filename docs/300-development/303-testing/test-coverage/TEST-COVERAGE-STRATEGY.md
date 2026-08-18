# Test Coverage Strategy & Automation

Comprehensive test coverage measurement and automated reporting across all layers.

## Current Coverage Status

| Layer | Type | Coverage | Target |
|-------|------|----------|--------|
| Backend (Java) | JaCoCo Instructions | 31% | 80% |
| Backend (Java) | JaCoCo Branches | 18% | 80% |
| Frontend (React) | Jest/Vitest | 45% | 70% |
| E2E (UI) | Playwright | 85% | 95% |
| API | Bruno CLI | 78% | 100% |

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
- Ratchet floor: 28% line, 14% branch (enforced via `mvn verify`)
- Target: 80% (aspirational)
- Excluded: Legacy `jpa` package

## Frontend Testing (React + Jest)

### Running Tests
```bash
npm test -- --coverage
npm test Button.test.tsx
open coverage/lcov-report/index.html
```

### Thresholds
- Branches: 70%
- Functions: 70%
- Lines: 70%
- Statements: 70%

## E2E Testing (Playwright)

### Running Tests
```bash
npx playwright test
npx playwright show-report
```

### Coverage
- **85%** of critical user flows
- Multi-browser: Chromium, Firefox, WebKit
- Interactive debugging: `--debug` flag

## API Testing (Bruno)

### Running Tests
```bash
bru run tests/api/bruno
bru run tests/api/bruno --json > results.json
```

### Coverage
- **50+ endpoints** tested
- **78%** coverage of all API routes
- **100%** coverage of critical flows (auth, CRUD)

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

See also: [AUDITORIA.md](../../AUDITORIA.md) — Mandatory test requirements
