# Notaire - Comprehensive Testing Strategy

This document describes the testing approach for the Notaire project across all layers: backend APIs, frontend clients, and end-to-end workflows.

## Overview

The project employs a **test pyramid** approach with multiple test types:

```
        ┌─────────────────────┐
        │   E2E Tests (7)     │  Robot Framework - Swing GUI
        │   ~30-40 cases      │
        └─────────────────────┘
              ▲       ▲
             / \     / \
            /   \   /   \
           /     \ /     \
        ┌─────────────────────┐
        │   HTTP Tests (8)    │  cURL/Bash - REST endpoints
        │   ~40-50 cases      │
        └─────────────────────┘
              ▲       ▲
             / \     / \
            /   \   /   \
           /     \ /     \
      ┌─────────────────────────────┐
      │ Integration Tests (~40)      │  Spring Boot TestContainers
      │ - API endpoints              │  H2 + PostgreSQL
      │ - Use case coverage          │
      │ - Report generation          │
      └─────────────────────────────┘
              ▲       ▲       ▲
             / \     / \     / \
            /   \   /   \   /   \
           /     \ /     \ /     \
      ┌─────────────────────────────┐
      │   Unit Tests (8+ classes)   │  JUnit 5
      │   - Entity tests             │  AssertJ
      │   - DTO mapping              │
      │   - Security/hashing         │
      └─────────────────────────────┘
```

---

## Test Distribution

| Layer | Type | Count | Framework | Duration | Purpose |
|-------|------|-------|-----------|----------|---------|
| **Unit** | Java/JUnit5 | 8+ classes | Spring Test | ~5s | Logic validation, fast feedback |
| **Integration** | Java/Spring | ~40 | TestContainers | ~30s | API contracts, database, transactions |
| **HTTP** | Bash/cURL | ~8 scripts | curl + assertions | ~15s | REST endpoint validation |
| **E2E** | Python/Robot | 7 suites | Robot Framework | ~5-10m | User workflows, GUI interaction |
| **Total** | Mixed | **100+** | - | **~10m** | Full coverage |

---

## 1. Unit Tests

### Location
`backend-api/src/test/java/com/licensis/notaire/unit/`

### Test Classes

| Class | Domain | Tests | Purpose |
|-------|--------|-------|---------|
| `PresupuestoEntityTest` | CU01 - Preparar Presupuesto | 8+ | Budget entity creation, validation, item management |
| `EscrituraEntityTest` | Documents | 6+ | Document entity lifecycle |
| `PersonaEntityTest` | Parties | 5+ | Person entity validation |
| `PagoEntityTest` | Payments | 5+ | Payment tracking |
| `GestionDeEscrituraEntityTest` | Document Management | 4+ | Workflow state transitions |
| `ConceptoDtoMappingTest` | DTOs | 4+ | DTO mapping correctness |
| `UsuarioControllerHashTest` | Authentication | 3+ | Password hashing security |
| `UseCaseRouteCatalogUnitTest` | Use Cases | 10+ | Use case catalog validation |

### Running Unit Tests

```bash
# Run all unit tests
mvn test -pl backend-api -Dtest="**/unit/*Test"

# Run specific test class
mvn test -pl backend-api -Dtest=PresupuestoEntityTest

# Run specific test method
mvn test -pl backend-api -Dtest=PresupuestoEntityTest#shouldCreatePresupuestoWithRequiredFields

# Run with detailed output
mvn test -pl backend-api -X
```

### Coverage Target
- **Minimum:** 80% line + branch coverage
- **Services covered:** Entity models, DTOs, authentication
- **Quick feedback:** ~5 seconds

---

## 2. Integration Tests

### Location
`backend-api/src/test/java/com/licensis/notaire/integration/`

### Test Suites

| Class | Database | Purpose |
|-------|----------|---------|
| `ApiH2IntegrationTest` | H2 in-memory | Fast API contract testing, no external deps |
| `ApiIntegrationTest` | PostgreSQL | Production-like database behavior |
| `UseCaseRouteCoverageIntegrationTest` | H2 | All use case routes covered by API |
| `UseCaseDomainsIntegrationTest` | PostgreSQL | Domain model relationships, constraints |
| `ReportesUseCaseIntegrationTest` | PostgreSQL | Report generation workflows |
| `BaseIntegrationTest` | Both | Base configuration, test fixtures |

### Key Features

- **TestContainers:** PostgreSQL spun up automatically in Docker
- **H2 Fallback:** Fast tests when database unavailable
- **Schema Migration:** Automatic via Hibernate (locally)
- **Fixtures:** Common test data (users, budgets, documents)
- **Transactions:** Automatic rollback per test

### Running Integration Tests

```bash
# Run all integration tests (H2 + PostgreSQL)
mvn test -pl backend-api -Dtest="**/integration/*IntegrationTest"

# Run only H2 tests (no Docker needed)
mvn test -pl backend-api -Dtest=ApiH2IntegrationTest

# Run with coverage
mvn test -pl backend-api -Dtest="**/integration/*IntegrationTest" jacoco:report

# Run with verbose logging
mvn test -pl backend-api -Dtest="**/integration/*IntegrationTest" -X
```

### Prerequisites

#### Option A: Docker + TestContainers (Recommended)
```bash
# Docker must be running
docker info

# TestContainers will automatically start PostgreSQL container
mvn test -pl backend-api
```

#### Option B: Local PostgreSQL
```bash
# If PostgreSQL 16 is running locally on 5432
mvn test -pl backend-api -Dspring.datasource.url=jdbc:postgresql://localhost:5432/notaire_test
```

#### Option C: H2 Only (No Setup)
```bash
# H2 tests run standalone
mvn test -pl backend-api -Dtest=ApiH2IntegrationTest
```

### Coverage Target
- **Minimum:** 80% line + branch coverage
- **Services covered:** Controllers, services, repositories
- **Slow but thorough:** ~30 seconds

---

## 3. Frontend Client Tests

### Location
`frontend-swing/src/test/java/com/licensis/notaire/api/client/`

### Test Classes

| Class | Purpose |
|-------|---------|
| `RestClientTest` | REST client initialization, HTTP methods (GET, POST, PUT, DELETE) |
| `ApiConfigTest` | API URL configuration, endpoint setup |

### Testing Approach

- **Mocking:** WireMock for HTTP stub server
- **No real API:** Tests run offline with mocked responses
- **Integration:** Full client behavior including error handling

### Running Client Tests

```bash
# Run all client tests
mvn test -pl frontend-swing

# Run specific test
mvn test -pl frontend-swing -Dtest=RestClientTest
```

---

## 4. HTTP Integration Tests

### Location
`integration-test/http/`

### Test Scripts

| Script | Coverage |
|--------|----------|
| `test-all-endpoints-v2.sh` | Comprehensive endpoint testing |
| `01-auth.sh` | Login, logout, token refresh |
| `02-usuarios.sh` | User CRUD operations |
| `03-conceptos.sh` | Concept/Item management |
| `04-personas.sh` | Person/Client management |
| `05-tramites.sh` | Process management |
| `06-escrituras.sh` | Document operations |
| `07-presupuestos.sh` | Budget workflows |
| `08-items.sh` | Item line management |

### What It Tests

- ✅ API authentication (JWT tokens)
- ✅ CRUD operations (Create, Read, Update, Delete)
- ✅ Data validation (field requirements)
- ✅ Error handling (404, 400, 500)
- ✅ Business workflows (budget → items)

### Running HTTP Tests

```bash
# Start backend first
bash scripts/start.sh  # or: mvn spring-boot:run -pl backend-api

# Run comprehensive tests
cd integration-test/http
bash test-all-endpoints-v2.sh

# Run specific endpoint tests
bash 01-auth.sh
bash 02-usuarios.sh

# Test single endpoint
curl -X GET http://localhost:8080/api/v1/presupuestos
```

### Prerequisites

Backend must be running on `http://localhost:8080`:

```bash
# Option A: Docker Compose
bash scripts/start.sh

# Option B: Local Maven
cd backend-api && mvn spring-boot:run

# Option C: IDE (Run com.licensis.notaire.NotaireApplication)
```

### Success Criteria
- All endpoints return expected status codes
- Response format is valid JSON
- Required fields present in responses
- CRUD operations work end-to-end

---

## 5. E2E Tests (Robot Framework)

### Location
`integration-test/e2e-swing/tests/`

### Test Suites

| File | Purpose | Test Cases |
|------|---------|-----------|
| `login_e2e.robot` | Authentication workflow | 3-4 |
| `principal_navigation_e2e.robot` | Main window navigation | 4-5 |
| `administracion_e2e.robot` | Admin panel, user roles | 4-5 |
| `clientes_e2e.robot` | Client/Person management | 5-6 |
| `gestiones_e2e.robot` | Process workflows | 4-5 |
| `presupuestos_e2e.robot` | Budget creation/editing | 5-6 |
| `protocolo_e2e.robot` | Document handling | 3-4 |

### What It Tests

- ✅ Login flow and authentication
- ✅ GUI navigation and menu items
- ✅ CRUD workflows through the UI
- ✅ User roles and permissions
- ✅ Business process workflows
- ✅ Error messages and validation feedback
- ✅ Integration between GUI and backend API

### Test Architecture

```
Robot Framework (tests/*.robot)
    │
    ├─ resources/common.resource (Shared keywords)
    │   ├─ Suite Setup: Ensure Backend Running, Launch Swing App
    │   ├─ Suite Teardown: Close Swing Application
    │   └─ Keywords: Type, Click, Assert, etc.
    │
    ├─ Test Cases (@Tags: smoke, login, e2e, agent-callable)
    │   ├─ Arrange (setup test data)
    │   ├─ Act (perform user actions)
    │   └─ Assert (verify outcomes)
    │
    └─ Swing Automation
        └─ JaycardGui library (Swing window detection)
        └─ Screenshot capture on failures
```

### Setup & Prerequisites

```bash
cd integration-test/e2e-swing

# 1. Create virtual environment
python -m venv .venv

# 2. Activate
source .venv/bin/activate  # on macOS/Linux
# or
.venv\Scripts\activate  # on Windows

# 3. Install dependencies
pip install -r requirements.txt

# 4. Ensure backend is running
bash ../../scripts/start.sh

# 5. Build Swing frontend JAR
mvn package -pl frontend-swing -DskipTests
```

### Running E2E Tests

```bash
source .venv/bin/activate

# Run all E2E tests
robot tests/

# Run specific suite
robot tests/login_e2e.robot

# Run specific test case
robot -t "Login With Valid Credentials Should Succeed" tests/login_e2e.robot

# Run with tags
robot --include smoke tests/
robot --include e2e tests/

# Generate HTML report
robot --outputdir results tests/
open results/report.html
```

### Test Output

Robot Framework generates:
- `report.html` - Execution summary
- `log.html` - Detailed test logs with timestamps
- `output.xml` - Machine-parseable results
- Screenshots on failure (named and timestamped)

### Success Criteria

- ✅ All test cases pass
- ✅ No exceptions in backend logs
- ✅ GUI responsive (no hangs/freezes)
- ✅ Data persists correctly in database
- ✅ Screenshots captured for troubleshooting

---

## Centralized Test Execution

### Master Test Script

Located at: `integration-test/run-all-tests.sh`

**Purpose:** Execute all test suites in sequence and generate consolidated report

### Usage

```bash
cd integration-test

# Run all tests (unit, integration, client, HTTP, E2E)
bash run-all-tests.sh

# Run only unit tests
bash run-all-tests.sh --unit-only

# Skip slow E2E tests
bash run-all-tests.sh --skip-robot

# Skip HTTP tests
bash run-all-tests.sh --skip-http

# Generate with coverage detail
bash run-all-tests.sh --coverage

# Help
bash run-all-tests.sh --help
```

### What It Does

1. **Discovers** test counts in each category
2. **Executes** tests in parallel where safe
3. **Collects** results and timings
4. **Generates** markdown report with:
   - Test counts per category
   - Pass/fail status
   - Execution times
   - Coverage metrics
   - Links to detailed logs

### Output

```
📊 Full Report: integration-test/reports/test-report-20260414_165832.md
📋 Logs Directory: integration-test/reports/logs/

Logs contain:
- unit-tests.log
- integration-tests.log
- client-tests.log
- http-tests.log
- e2e-tests.log
- robot-output/ (with HTML reports)
```

---

## Code Coverage Analysis

### JaCoCo Coverage Reporting

Located at: `integration-test/generate-coverage-report.sh`

**Purpose:** Analyze code coverage by package/service

### Usage

```bash
cd integration-test

# Generate coverage report
bash generate-coverage-report.sh

# Generate with HTML output
bash generate-coverage-report.sh --html

# Check against 80% threshold
bash generate-coverage-report.sh --threshold 80
```

### Reports Generated

```
integration-test/reports/
├── coverage-report.md       (Markdown analysis)
├── coverage.txt             (Coverage percentage)
└── logs/
    └── coverage-details/    (Per-package breakdown)
```

### Viewing Coverage

```bash
# Open HTML coverage report (from Maven)
open backend-api/target/site/jacoco/index.html

# View markdown report
cat integration-test/reports/coverage-report.md
```

### Coverage Requirements

```
Minimum Thresholds:
├── Line Coverage:   80%
├── Branch Coverage: 80%
├── Packages:        All packages monitored
└── Failures:        CI blocks merge if below threshold
```

---

## CI/CD Integration

Tests run automatically in GitHub Actions on:

1. **Pull Requests**
   - Unit tests (5s)
   - Integration tests with H2 (30s)
   - Code style checks (Checkstyle)
   - Bug detection (SpotBugs)

2. **Merge to Main**
   - All unit + integration tests
   - Coverage report (must meet 80%)
   - Security scanning (Trivy)

3. **Release Builds**
   - Full test suite including E2E
   - Docker image security scan
   - Artifact generation

---

## Quick Reference Commands

### Run Everything
```bash
# Unit only (fastest, ~5s)
mvn test -pl backend-api

# Unit + Integration (~30s)
mvn verify -pl backend-api

# Unit + Integration + Coverage (slower, ~45s)
mvn clean test jacoco:report -pl backend-api

# All tests including E2E (~15m)
cd integration-test && bash run-all-tests.sh
```

### View Results
```bash
# Coverage HTML
open backend-api/target/site/jacoco/index.html

# Test report HTML (Robot)
open integration-test/reports/logs/robot-output/report.html

# Consolidated markdown report
cat integration-test/reports/test-report-*.md
```

### Troubleshooting

```bash
# Check test failures
cat integration-test/reports/logs/unit-tests.log

# Verify PostgreSQL running
docker ps | grep postgres

# View API logs during HTTP tests
bash scripts/logs.sh

# Debug E2E test
robot --loglevel DEBUG tests/login_e2e.robot

# Check coverage gaps
grep -B2 "0.0%" backend-api/target/site/jacoco/index.html
```

---

## Best Practices

### Writing Tests

✅ **DO:**
- Use descriptive method names: `shouldCreatePresupuestoWithValidItems()`
- Add `@DisplayName` with full description
- Test one concern per test method
- Use AssertJ fluent assertions
- Group related tests in nested classes

❌ **DON'T:**
- Skip exceptions with try-catch without failing
- Test multiple concerns in one method
- Use generic variable names (x, y, obj)
- Hard-code test data in assertions
- Ignore intermittent failures

### Integration Test Data

- Use fixtures/builders for test data
- Isolate tests (each is independent)
- Use transactions for cleanup (automatic)
- Don't rely on test execution order

### E2E Test Maintenance

- Update screenshots when UI changes
- Keep test data aligned with backend
- Use tags for test categorization
- Document special setup steps
- Monitor for flakiness

---

## Next Steps

1. **Run full test suite:**
   ```bash
   cd integration-test && bash run-all-tests.sh
   ```

2. **Review coverage report:**
   ```bash
   open backend-api/target/site/jacoco/index.html
   ```

3. **Add missing tests** for new features

4. **Configure CI/CD** for automated testing

5. **Monitor test trends** over time

---

**Last Updated:** 2026-04-14
**Maintained By:** Development Team
**Version:** 1.0
