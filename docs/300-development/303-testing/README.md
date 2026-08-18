# Notaire Integration Tests & Test Automation

Centralized testing framework for the Notaire project, covering unit, component, API,
integration, Playwright UI/UX, and system-level E2E testing.

## Test Plan & Use-Case Traceability

The project's testing pyramid runs from isolated unit tests up to system-level E2E
suites that validate each **Caso de Uso (Use Case)** end to end:

```
Unit  ->  Component  ->  API (Bruno)  ->  Integration  ->  Playwright UI/UX  ->  E2E (system, per use case)
```

| Document | Defines |
|----------|---------|
| [TEST_STRATEGY.md](TEST_STRATEGY.md) | The comprehensive test suite: unit, integration, frontend client, and HTTP/API tests, with commands, coverage targets, and CI wiring |
| [E2E-TEST-PLAN.md](E2E-TEST-PLAN.md) | Playwright + Bruno E2E test plan: architecture, test data strategy, and suite organization **per use case** (`cuNN-*.spec.ts`) |
| [CU-API-MATRIX.csv](CU-API-MATRIX.csv) | Traceability matrix: Caso de Uso ID -> module -> entity/operation -> controller/endpoint -> Bruno test -> GitHub issue. `Endpoint_Status`/`Bruno_Status` are a historical snapshot from initial bug-hunting (issues #154-#221); for current Bruno pass/fail state see [`api-test/COVERAGE.md`](../../../backend-api/api-test/COVERAGE.md) |
| [FRONTEND-TESTING-GUIDE.md](FRONTEND-TESTING-GUIDE.md) | Vitest unit/component testing conventions for `frontend/` |

Test reports are generated per run (see [Reports & Outputs](#reports--outputs) below) —
Markdown/HTML for the backend suite (`testing/reports/`), JaCoCo HTML for coverage, and
Playwright's own HTML report for the E2E suite (`frontend/playwright-report/`).

## Quick Start

### Run All Tests in 3 Steps

```bash
# 1. Navigate to testing directory
cd testing

# 2. Execute centralized test runner
bash run-all-tests.sh

# 3. View comprehensive report
cat reports/test-report-*.md
```

**Expected Output:**
- Test counts per type
- Pass/fail status
- Execution times
- Coverage metrics
- Links to detailed logs

---

## Test Types

### 1. **Unit Tests** (Java/JUnit5)
- **Location:** `backend-api/src/test/java/.../unit/`
- **Count:** 8+ test classes
- **Duration:** ~5 seconds
- **Command:** `mvn test -pl backend-api -Dtest="**/unit/*"`

### 2. **Integration Tests** (Spring Boot/TestContainers)
- **Location:** `backend-api/src/test/java/.../integration/`
- **Count:** ~40 test cases
- **Duration:** ~30 seconds
- **Command:** `mvn test -pl backend-api -Dtest="**/integration/*"`
- **Databases:** H2 (fast) + PostgreSQL (production-like)

### 3. **Client Tests** (REST API Client, deprecated Swing)
- **Location:** `deprecated-frontend-swing/src/test/java/.../api/client/`
- **Count:** 2 test classes
- **Duration:** ~2 seconds
- **Command:** `cd deprecated-frontend-swing && mvn test` (the module is excluded from
  the root Maven reactor, so `-pl` from repo root will not resolve it — see issue #811)

### 4. **HTTP Integration Tests** (cURL/Bash)
- **Location:** `http/` directory
- **Scripts:** 8 endpoint test scripts
- **Duration:** ~15 seconds
- **Command:** `bash http/test-all-endpoints-v2.sh`
- **Prerequisites:** Backend running on `http://localhost:8080`

### 5. **Frontend Unit/Component Tests** (Vitest, Next.js)
- **Location:** `frontend/src/**/*.test.ts(x)`
- **Count:** 19+ test files
- **Duration:** seconds
- **Command:** `cd frontend && npm test` (`npm run test:coverage` for coverage)

### 6. **Playwright E2E Tests** (UI/UX, use-case validation)
- **Location:** `frontend/tests/e2e/`
- **Count:** 33+ spec files, organized by use case (`cuNN-*.spec.ts`)
- **Command:** `cd frontend && npm run test:e2e` (`npm run test:e2e:headed` to watch)
- **Prerequisites:** Full stack running (`bash scripts/start.sh`)
- **Test plan:** see [E2E-TEST-PLAN.md](E2E-TEST-PLAN.md) for suite organization,
  test data strategy, and the CI schedule (`playwright-e2e.yml`)

### 7. **E2E Tests** (Robot Framework/Swing, deprecated client)
- **Location:** `e2e-swing/tests/` directory
- **Suites:** 7 Robot test files (~30-40 test cases)
- **Duration:** ~5-10 minutes
- **Command:** `cd e2e-swing && robot tests/`
- **Prerequisites:** Backend running, Swing JAR built, Python venv

---

## Detailed Usage

### All Tests (Complete Suite)

```bash
bash run-all-tests.sh
```

**Options:**
- `--unit-only` — Run only unit tests (fastest)
- `--skip-robot` — Skip slow E2E tests
- `--skip-http` — Skip HTTP tests
- `--coverage` — Generate detailed coverage report

### Unit Tests Only (5 seconds)

```bash
# Run all unit tests
mvn test -pl backend-api -Dtest="**/unit/*"

# Run specific test class
mvn test -pl backend-api -Dtest=PresupuestoEntityTest

# Run specific test method
mvn test -pl backend-api -Dtest=PresupuestoEntityTest#shouldCreatePresupuestoWithRequiredFields
```

### Integration Tests (30 seconds)

```bash
# Run all integration tests (H2 + PostgreSQL)
mvn test -pl backend-api -Dtest="**/integration/*"

# Run only H2 tests (no Docker needed)
mvn test -pl backend-api -Dtest=ApiH2IntegrationTest

# Run with coverage
mvn test -pl backend-api jacoco:report
```

### API Client Tests

```bash
# Frontend Swing client tests (module excluded from root reactor; run from its own directory)
cd deprecated-frontend-swing && mvn test
```

### HTTP Integration Tests

```bash
cd testing/http

# Requires backend running on localhost:8080
bash test-all-endpoints-v2.sh

# Or run individual endpoint tests
bash 01-auth.sh
bash 02-usuarios.sh
bash 03-conceptos.sh
```

### E2E Tests (5-10 minutes)

```bash
cd testing/e2e-swing

# Setup (one time)
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt

# Run all tests
robot tests/

# Run specific suite
robot tests/login_e2e.robot

# Run with tags
robot --include smoke tests/

# View results
open results/report.html
```

---

## Prerequisites

### All Tests
- Java 21+
- Maven 3.8+
- Git

### Integration Tests
- Docker (for TestContainers PostgreSQL)
- Or local PostgreSQL on port 5432

### E2E Tests
- Python 3.8+
- Swing JAR built: `cd deprecated-frontend-swing && mvn package`
- Backend running: `bash scripts/start.sh`

### HTTP Tests
- curl (usually pre-installed)
- Backend running: `bash scripts/start.sh` or `mvn spring-boot:run -pl backend-api`

---

## Test Counts & Coverage

| Type | Count | Coverage | Duration |
|------|-------|----------|----------|
| Unit | 8+ classes | Service logic | ~5s |
| Integration | ~40 cases | APIs, database | ~30s |
| Client | 2 classes | REST client | ~2s |
| HTTP | 8 scripts | REST endpoints | ~15s |
| E2E | ~30-40 cases | GUI workflows | ~10m |
| **Total** | **100+** | **Full stack** | **~15m** |

**Coverage Requirement:** Enforced ratchet floor via JaCoCo (`jacoco:check`, bound to `mvn verify`; raised as coverage improves, never lowered — see `.claude/rules/code-quality.md`). Long-term target: 80% line / 80% branch.

---

## Reports & Outputs

After running tests, find reports at:

```
testing/
├── reports/
│   ├── test-report-*.md           ← Consolidated markdown report
│   ├── coverage-report.md         ← Code coverage analysis
│   ├── coverage.txt               ← Coverage percentage
│   └── logs/
│       ├── unit-tests.log
│       ├── integration-tests.log
│       ├── client-tests.log
│       ├── http-tests.log
│       ├── e2e-tests.log
│       └── robot-output/          ← Robot Framework HTML report
│           ├── report.html
│           ├── log.html
│           └── output.xml
```

**View coverage in browser:**
```bash
open backend-api/target/site/jacoco/index.html
```

---

## Test Organization

### By Domain
- **Presupuesto (Budget):** CU01, items, workflows
- **Escritura (Document):** CRUD, state management
- **Persona (Client):** Person entities, relationships
- **Usuario (User):** Authentication, roles, permissions
- **Tramite (Process):** Workflow orchestration

### By Test Type
- **Unit:** Entity creation, DTO mapping, security hashing
- **Integration:** API contracts, database transactions, workflows
- **HTTP:** REST endpoint validation, error handling
- **E2E:** User workflows, GUI interaction, full stack

### By Naming
- Test methods: `shouldXxxYyy` format
- Test classes: `*Test` or `*IntegrationTest` suffix
- E2E: `*_e2e.robot` files

---

## Common Tasks

### Run Tests Before Committing
```bash
# Fast smoke test (~10s)
mvn test -pl backend-api

# Include integration tests (~45s)
mvn verify -pl backend-api

# Full pipeline with coverage
mvn clean test jacoco:report -pl backend-api
```

### Check Code Coverage
```bash
# Generate report
mvn clean test jacoco:report -pl backend-api

# View HTML
open backend-api/target/site/jacoco/index.html

# Check against the enforced ratchet floor
mvn jacoco:check -pl backend-api
```

### Debug Failing Test
```bash
# Run with full output
mvn test -pl backend-api -X -e

# View logs
tail -f testing/reports/logs/*.log

# Check screenshots (E2E)
open testing/reports/logs/robot-output/FAIL_*.png
```

### View Test Summary
```bash
# Markdown report
cat testing/reports/test-report-*.md

# HTML report (E2E)
open testing/reports/logs/robot-output/report.html

# Last line of output
grep -E "PASSED|FAILED" testing/reports/logs/*.log
```

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────┐
│            E2E Tests (Robot Framework)           │
│        - Login, Navigation, Workflows            │
│     ✓ Validates full GUI + API integration      │
└──────────┬──────────────────────────────────────┘
           │
      ┌────▼─────┐
      │  Swing   │
      │   GUI    │
      └────┬─────┘
           │ HTTP
      ┌────▼────────────────────────┐
      │   HTTP API Tests (cURL)     │
      │ ✓ Endpoint validation        │
      │ ✓ Error handling             │
      └────┬──────────────────┬──────┘
           │                  │
    ┌──────▼────────┐    ┌────▼──────────────┐
    │ Integration   │    │  Client Tests     │
    │ Tests (Spring)│    │  (REST Client)    │
    │ ✓ API impl    │    │ ✓ Mocked server   │
    │ ✓ Database    │    │ ✓ Config & errors │
    │ ✓ Workflows   │    │                   │
    └──────┬────────┘    └───────────────────┘
           │
    ┌──────▼──────────────┐
    │   Unit Tests        │
    │   (JUnit5)          │
    │ ✓ Entity logic      │
    │ ✓ DTO mapping       │
    │ ✓ Security hashing  │
    └─────────────────────┘
           ▲
           │
    ┌──────┴──────────────┐
    │  Backend API        │
    │  (Spring Boot)      │
    │  Services/Repos     │
    └─────────────────────┘
```

---

## CI/CD Integration

Tests run automatically in GitHub Actions:

**On Pull Request:**
- Unit tests (5s)
- Integration tests with H2 (30s)
- Code style (Checkstyle)
- Bug detection (SpotBugs)

**On Merge to Main:**
- All unit + integration tests
- Coverage report (enforced ratchet floor; 80% target)
- Security scan (Trivy)

**On Release:**
- Full test suite including E2E
- Docker image scan
- Artifact generation

---

## Documentation

For detailed information, see:

- `TEST_STRATEGY.md` — Comprehensive testing guide
- `API_TESTING_GUIDE.md` — HTTP API testing details
- `backend-api/pom.xml` — Maven test configuration
- `deprecated-frontend-swing/pom.xml` — Client test configuration
- `e2e-swing/README.md` — E2E setup instructions

---

## Scripts in This Directory

| Script | Purpose |
|--------|---------|
| `run-all-tests.sh` | Execute all test suites and generate report |
| `generate-coverage-report.sh` | Analyze code coverage by package |
| `http/test-all-endpoints-v2.sh` | Comprehensive HTTP endpoint testing |
| `e2e-swing/` | Robot Framework E2E tests |
| `integration/` | Integration test helpers |

---

## Quick Links

- **Coverage Report:** `open backend-api/target/site/jacoco/index.html`
- **E2E Report:** `open testing/reports/logs/robot-output/report.html`
- **API Guide:** [`api-test/API_TESTING_GUIDE.md`](api-test/API_TESTING_GUIDE.md)
- **CLAUDE.md:** `../.claude.md` (project testing guidelines)

---

## Troubleshooting

### Tests Won't Run
```bash
# Check Java version
java -version  # Should be 21+

# Check Maven
mvn --version  # Should be 3.8+

# Clean and rebuild
mvn clean
```

### PostgreSQL Connection Error
```bash
# Start Docker services
bash scripts/start.sh

# Or verify port 5432
lsof -i :5432
```

### E2E Tests Failing
```bash
# Rebuild Swing JAR
cd deprecated-frontend-swing && mvn package -DskipTests

# Ensure backend is running
curl http://localhost:8080/swagger-ui.html

# Check Python environment
source e2e-swing/.venv/bin/activate
python -c "import robot; print(robot.__version__)"
```

### Coverage Below the Ratchet Floor
```bash
# View detailed coverage
open backend-api/target/site/jacoco/index.html

# Add tests for red lines
# Commit and re-run
mvn jacoco:check -pl backend-api
```

---

## Getting Help

1. Check `TEST_STRATEGY.md` for detailed guidance
2. Review test logs: `testing/reports/logs/`
3. Search for similar tests in existing test files
4. Check GitHub Issues for known problems
5. Run with verbose flags: `-X`, `-v`, `--debug`

---

**Last Updated:** 2026-04-14  
**Maintained By:** Development Team  
**Version:** 1.0
