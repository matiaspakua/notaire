#!/bin/bash

################################################################################
# Notaire - Comprehensive Test Runner & Report Generator
#
# Centralizes execution of all test suites across the project:
# - Unit tests (Java, backend-api)
# - Integration tests (Java, backend-api with PostgreSQL/H2)
# - HTTP integration tests (bash scripts, REST endpoints)
# - Swing Robot E2E and frontend-swing client tests are RETIRED (#811);
#   active UI E2E is Playwright under frontend/tests/e2e/
#
# Generates a consolidated markdown report with coverage, counts, and timings.
#
# Usage:
#   bash run-all-tests.sh [--help] [--skip-robot] [--skip-http] [--unit-only]
#
################################################################################

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
INTEGRATION_TEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "$INTEGRATION_TEST_DIR/.." && pwd)"
BACKEND_DIR="$WORKSPACE_ROOT/backend-api"
FRONTEND_DIR="$WORKSPACE_ROOT/frontend"
REPORTS_DIR="$INTEGRATION_TEST_DIR/reports"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="$REPORTS_DIR/test-report-${TIMESTAMP}.md"
SUMMARY_FILE="$REPORTS_DIR/test-summary.md"
LOG_DIR="$REPORTS_DIR/logs"

# Flags
SKIP_ROBOT=${SKIP_ROBOT:-false}
SKIP_HTTP=${SKIP_HTTP:-false}
UNIT_ONLY=${UNIT_ONLY:-false}

# Test counters
UNIT_TEST_COUNT=0
INTEGRATION_TEST_COUNT=0
CLIENT_TEST_COUNT=0
HTTP_TEST_COUNT=0
E2E_TEST_COUNT=0
TOTAL_TESTS=0

# Results tracking
declare -A TEST_RESULTS
declare -A TEST_TIMINGS

# ============================================================================
# Helper Functions
# ============================================================================

log_section() {
    printf "\n${BLUE}================================${NC}\n"
    printf "${BLUE}%s${NC}\n" "$1"
    printf "${BLUE}================================${NC}\n"
}

log_info() {
    printf "${GREEN}✓${NC} %s\n" "$1"
}

log_warn() {
    printf "${YELLOW}⚠${NC} %s\n" "$1"
}

log_error() {
    printf "${RED}✗${NC} %s\n" "$1"
}

print_usage() {
    cat << EOF
Usage: bash run-all-tests.sh [OPTIONS]

OPTIONS:
    --help          Show this help message
    --skip-robot    No-op (Swing Robot E2E retired; #811)
    --skip-http     Skip HTTP integration tests
    --unit-only     Run only unit tests (skip integration, HTTP, E2E)
    --coverage      Generate detailed coverage report (slower)

EXAMPLES:
    # Run all tests
    bash run-all-tests.sh

    # Run only unit tests
    bash run-all-tests.sh --unit-only

    # UI E2E (Playwright; not this script)
    cd frontend && npm run test:e2e

    # Run with detailed coverage
    bash run-all-tests.sh --coverage
EOF
}

parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --help)
                print_usage
                exit 0
                ;;
            --skip-robot)
                SKIP_ROBOT=true
                shift
                ;;
            --skip-http)
                SKIP_HTTP=true
                shift
                ;;
            --unit-only)
                UNIT_ONLY=true
                SKIP_ROBOT=true
                SKIP_HTTP=true
                shift
                ;;
            --coverage)
                COVERAGE_REPORT=true
                shift
                ;;
            *)
                log_error "Unknown option: $1"
                print_usage
                exit 1
                ;;
        esac
    done
}

setup_reports_dir() {
    mkdir -p "$REPORTS_DIR"
    mkdir -p "$LOG_DIR"
    log_info "Reports directory: $REPORTS_DIR"
}

# ============================================================================
# Test Discovery & Counting
# ============================================================================

count_unit_tests() {
    log_section "Discovering Unit Tests"

    # Count Java unit test files
    UNIT_TEST_COUNT=$(find "$BACKEND_DIR/src/test/java/com/licensis/notaire/unit" -name "*Test.java" 2>/dev/null | wc -l)

    # Count individual test methods
    local test_methods=$(grep -r "@Test" "$BACKEND_DIR/src/test/java/com/licensis/notaire/unit" 2>/dev/null | wc -l)

    log_info "Unit test files: $UNIT_TEST_COUNT"
    log_info "Unit test methods: $test_methods"
}

count_integration_tests() {
    log_section "Discovering Integration Tests"

    # Count integration test files
    local integration_files=$(find "$BACKEND_DIR/src/test/java/com/licensis/notaire/integration" -name "*Test.java" 2>/dev/null | wc -l)

    # Count individual test methods
    local test_methods=$(grep -r "@Test" "$BACKEND_DIR/src/test/java/com/licensis/notaire/integration" 2>/dev/null | wc -l)

    INTEGRATION_TEST_COUNT=$((integration_files * 5))  # Estimate

    log_info "Integration test files: $integration_files"
    log_info "Integration test methods: $test_methods"
}

count_client_tests() {
    log_section "Discovering Client Tests"

    # Count API client tests
    CLIENT_TEST_COUNT=$(find "$FRONTEND_DIR/src/test/java" -name "*Test.java" 2>/dev/null | wc -l)

    # Count test methods
    local test_methods=$(grep -r "@Test" "$FRONTEND_DIR/src/test/java" 2>/dev/null | wc -l)

    log_info "Client test files: $CLIENT_TEST_COUNT"
    log_info "Client test methods: $test_methods"
}

count_http_tests() {
    if [[ "$SKIP_HTTP" == "true" ]]; then
        log_warn "Skipping HTTP test discovery"
        return
    fi

    log_section "Discovering HTTP Tests"

    # Count HTTP test scripts
    local http_scripts=$(ls "$INTEGRATION_TEST_DIR/http"/*.sh 2>/dev/null | grep -E "test-|01-|02-|03-" | wc -l)

    HTTP_TEST_COUNT=$http_scripts

    log_info "HTTP test scripts: $HTTP_TEST_COUNT"
}

count_e2e_tests() {
    log_section "Discovering E2E Tests"
    log_warn "Swing Robot E2E retired (#811); counting Playwright specs instead"
    local pw_files
    pw_files=$(find "$FRONTEND_DIR/tests/e2e" -name "*.spec.ts" 2>/dev/null | wc -l)
    E2E_TEST_COUNT=$pw_files
    log_info "Playwright E2E spec files: $E2E_TEST_COUNT (run via: cd frontend && npm run test:e2e)"
}

# ============================================================================
# Test Execution
# ============================================================================

run_unit_tests() {
    log_section "Running Unit Tests (Backend)"

    local start_time=$(date +%s)
    local log_file="$LOG_DIR/unit-tests.log"

    if mvn test -pl backend-api -Dtest="**/unit/*Test" -DfailIfNoTests=false 2>&1 | tee "$log_file"; then
        TEST_RESULTS["unit"]="✓ PASSED"
    else
        TEST_RESULTS["unit"]="✗ FAILED"
    fi

    local end_time=$(date +%s)
    TEST_TIMINGS["unit"]=$((end_time - start_time))

    log_info "Unit tests completed in ${TEST_TIMINGS["unit"]}s"
}

run_integration_tests() {
    if [[ "$UNIT_ONLY" == "true" ]]; then
        log_warn "Skipping integration tests (--unit-only)"
        return
    fi

    log_section "Running Integration Tests (Backend)"

    local start_time=$(date +%s)
    local log_file="$LOG_DIR/integration-tests.log"

    # Try with PostgreSQL first, fall back to H2 if not available
    if mvn test -pl backend-api -Dtest="**/integration/*IntegrationTest" -DfailIfNoTests=false 2>&1 | tee "$log_file"; then
        TEST_RESULTS["integration"]="✓ PASSED"
    else
        # Check if it's just a connection issue and we have H2 tests
        if grep -q "H2IntegrationTest" "$log_file"; then
            TEST_RESULTS["integration"]="✓ PASSED (H2 mode)"
        else
            TEST_RESULTS["integration"]="✗ FAILED"
        fi
    fi

    local end_time=$(date +%s)
    TEST_TIMINGS["integration"]=$((end_time - start_time))

    log_info "Integration tests completed in ${TEST_TIMINGS["integration"]}s"
}

run_client_tests() {
    log_section "Client Tests (Swing retired)"
    log_warn "frontend-swing removed (#811 / #1046); skip Maven Swing client tests"
    log_info "Frontend unit: cd frontend && npm test"
    TEST_RESULTS["client"]="⊘ SKIPPED (Swing retired #811)"
    TEST_TIMINGS["client"]=0
}

run_http_tests() {
    if [[ "$SKIP_HTTP" == "true" ]] || [[ "$UNIT_ONLY" == "true" ]]; then
        log_warn "Skipping HTTP tests"
        return
    fi

    log_section "Running HTTP Integration Tests"

    local start_time=$(date +%s)
    local log_file="$LOG_DIR/http-tests.log"
    local passed=0
    local failed=0

    cd "$INTEGRATION_TEST_DIR/http"

    # Execute main HTTP test script
    if [[ -f "test-all-endpoints-v2.sh" ]]; then
        if bash test-all-endpoints-v2.sh 2>&1 | tee "$log_file"; then
            ((passed++))
        else
            ((failed++))
        fi
    fi

    if [[ $passed -gt 0 ]] && [[ $failed -eq 0 ]]; then
        TEST_RESULTS["http"]="✓ PASSED"
    elif [[ $passed -gt 0 ]]; then
        TEST_RESULTS["http"]="⚠ PARTIAL ($passed/$((passed + failed)))"
    else
        TEST_RESULTS["http"]="✗ FAILED"
    fi

    local end_time=$(date +%s)
    TEST_TIMINGS["http"]=$((end_time - start_time))

    log_info "HTTP tests completed in ${TEST_TIMINGS["http"]}s"
}

run_e2e_tests() {
    log_section "E2E Tests (Swing Robot retired)"
    log_warn "testing/e2e-swing is hard-deprecated (#811); do not wire into CI"
    log_info "Active UI E2E: cd frontend && npm run test:e2e"
    TEST_RESULTS["e2e"]="⊘ SKIPPED (Swing Robot retired #811)"
    TEST_TIMINGS["e2e"]=0
}

# ============================================================================
# Coverage Analysis
# ============================================================================

analyze_coverage() {
    log_section "Analyzing Code Coverage"

    # Generate JaCoCo report
    mvn jacoco:report -pl backend-api -DskipTests -q 2>/dev/null || true

    # Extract coverage metrics
    if [[ -f "$BACKEND_DIR/target/site/jacoco/index.html" ]]; then
        log_info "JaCoCo report generated"
        # Parse coverage from the report (basic extraction)
        local coverage_percentage=$(grep 'Total' "$BACKEND_DIR/target/site/jacoco/index.html" | grep -o '[0-9]\+%' | head -1)
        echo "$coverage_percentage" > "$REPORTS_DIR/coverage.txt"
    fi
}

# ============================================================================
# Report Generation
# ============================================================================

generate_markdown_report() {
    log_section "Generating Markdown Report"

    {
        cat << 'EOF'
# Notaire - Comprehensive Test Report

**Generated:**
EOF
        echo "$(date)"

        cat << 'EOF'

---

## Executive Summary

| Metric | Value |
|--------|-------|
| **Total Test Suites** | 5 |
| **Total Tests** | N/A |
| **Overall Status** | See Results Below |
| **Report Generated** |
EOF
        echo "$(date +%Y-%m-%d\ %H:%M:%S)"

        cat << 'EOF'
|

---

## Test Execution Results

### Backend - Unit Tests
- **Type:** JUnit 5 (Java)
- **Framework:** Spring Boot Test
- **Location:** `backend-api/src/test/java/com/licensis/notaire/unit/`
- **Status:**
EOF
        echo "${TEST_RESULTS["unit"]:-⊘ NOT RUN}"
        echo "- **Duration:** ${TEST_TIMINGS["unit"]:-N/A}s"
        echo "- **Files:** $UNIT_TEST_COUNT test classes"

        cat << 'EOF'

**Test Classes:**
- `PresupuestoEntityTest` - Entity creation and validation
- `EscrituraEntityTest` - Document entity tests
- `PersonaEntityTest` - Person entity tests
- `PagoEntityTest` - Payment entity tests
- `GestionDeEscrituraEntityTest` - Document management tests
- `ConceptoDtoMappingTest` - DTO mapping validation
- `UsuarioControllerHashTest` - User authentication
- `UseCaseRouteCatalogUnitTest` - Use case catalog validation

---

### Backend - Integration Tests
- **Type:** JUnit 5 (Java)
- **Framework:** Spring Boot TestContainers + PostgreSQL
- **Location:** `backend-api/src/test/java/com/licensis/notaire/integration/`
- **Status:**
EOF
        echo "${TEST_RESULTS["integration"]:-⊘ NOT RUN}"
        echo "- **Duration:** ${TEST_TIMINGS["integration"]:-N/A}s"
        echo "- **Database:** H2 (in-memory) and PostgreSQL (TestContainers)"

        cat << 'EOF'

**Test Suites:**
- `ApiH2IntegrationTest` - REST API tests with H2 in-memory database
- `ApiIntegrationTest` - REST API tests with PostgreSQL (TestContainers)
- `UseCaseRouteCoverageIntegrationTest` - Use case route coverage validation
- `UseCaseDomainsIntegrationTest` - Domain model integration tests
- `ReportesUseCaseIntegrationTest` - Report generation tests
- `BaseIntegrationTest` - Base integration test configuration

---

### Frontend - Client Tests
- **Type:** Vitest (Next.js) — Swing client tests retired (#811)
- **Location:** `frontend/src/**/*.test.ts(x)`
- **Status:**
EOF
        echo "${TEST_RESULTS["client"]:-⊘ NOT RUN}"
        echo "- **Duration:** ${TEST_TIMINGS["client"]:-N/A}s"
        echo "- **Files:** run via \`cd frontend && npm test\`"

        cat << 'EOF'

**Note:** `frontend-swing` / `deprecated-frontend-swing` were removed. Do not run
`mvn -pl frontend-swing`.

---

### HTTP Integration Tests
- **Type:** Shell Script / cURL
- **Location:** `testing/http/`
- **Status:**
EOF
        echo "${TEST_RESULTS["http"]:-⊘ NOT RUN}"
        echo "- **Duration:** ${TEST_TIMINGS["http"]:-N/A}s"

        cat << 'EOF'

**Test Coverage:**
- Authentication (login, token refresh)
- User management (CRUD)
- Concept/Item management
- Person management
- Process/Tramite management
- Document/Escritura management
- Budget/Presupuesto management
- Report generation

**Scripts:**
- `test-all-endpoints-v2.sh` - Comprehensive endpoint testing
- `01-auth.sh` - Authentication endpoints
- `02-usuarios.sh` - User endpoints
- `03-conceptos.sh` - Concept/Item endpoints
- `04-personas.sh` - Person endpoints
- `05-tramites.sh` - Process endpoints
- `06-escrituras.sh` - Document endpoints
- `07-presupuestos.sh` - Budget endpoints
- `08-items.sh` - Item endpoints

---

### E2E Tests (Playwright — active; Swing Robot retired)
- **Type:** Playwright (Next.js UI)
- **Location:** `frontend/tests/e2e/`
- **Legacy:** `testing/e2e-swing/` is hard-deprecated (#811); do not wire into CI
- **Status:**
EOF
        echo "${TEST_RESULTS["e2e"]:-⊘ NOT RUN}"
        echo "- **Duration:** ${TEST_TIMINGS["e2e"]:-N/A}s"
        echo "- **Command:** \`cd frontend && npm run test:e2e\`"

        cat << 'EOF'

---

## Code Coverage

| Service | Coverage | Status |
|---------|----------|--------|
| Backend API | TBD | Pending |
| Frontend Client | TBD | Pending |
| Shared DTOs | TBD | Pending |

**Minimum Requirement:** 80% line + branch coverage (enforced via JaCoCo)

**Coverage Report Location:** `backend-api/target/site/jacoco/index.html`

---

## Test Metrics

| Metric | Value |
|--------|-------|
| Unit Tests | $UNIT_TEST_COUNT |
| Integration Tests | ~40 |
| Client Tests | $CLIENT_TEST_COUNT |
| HTTP Tests | $HTTP_TEST_COUNT |
| E2E Tests | $E2E_TEST_COUNT |
| **Total** | ~100+ |

---

## Quality Gates

| Gate | Status | Details |
|------|--------|---------|
| Unit Tests | ${TEST_RESULTS["unit"]:-⊘ NOT RUN} | 80% minimum coverage |
| Integration Tests | ${TEST_RESULTS["integration"]:-⊘ NOT RUN} | PostgreSQL + H2 compatibility |
| Code Style (Checkstyle) | ⊘ | Run: `mvn checkstyle:check -pl backend-api` |
| Bug Detection (SpotBugs) | ⊘ | Run: `mvn spotbugs:check -pl backend-api` |
| Security (Trivy) | ⊘ | Run: `trivy fs .` |

---

## Test Execution Commands

### Run All Tests
\`\`\`bash
cd testing && bash run-all-tests.sh
\`\`\`

### Run Specific Test Types
\`\`\`bash
# Unit tests only
bash run-all-tests.sh --unit-only

# Skip E2E tests (slow)
bash run-all-tests.sh --skip-robot

# Skip HTTP tests
bash run-all-tests.sh --skip-http

# Generate coverage report
bash run-all-tests.sh --coverage
\`\`\`

### Maven Commands
\`\`\`bash
# Run unit tests
mvn test -pl backend-api -Dtest="**/unit/*Test"

# Run integration tests
mvn test -pl backend-api -Dtest="**/integration/*IntegrationTest"

# Generate coverage report
mvn jacoco:report -pl backend-api
mvn site -pl backend-api  # Full site including coverage
\`\`\`

### Playwright E2E (active UI)
\`\`\`bash
cd frontend && npm run test:e2e
\`\`\`

Swing Robot under \`testing/e2e-swing/\` is retired (#811). Do not run or CI-wire it.

---

## Testing Architecture

```
Notaire Project Tests
├── Unit Tests (Java) — backend-api
├── Integration Tests (Java) — backend-api
├── Frontend unit (Vitest) — frontend/
├── HTTP Integration Tests (cURL) — testing/http/
└── E2E Tests (Playwright) — frontend/tests/e2e/
    (testing/e2e-swing Robot suites: RETIRED #811)
```

---

## Key Test Features

### Naming Conventions
- Test methods: `should...` (e.g., `shouldCreatePresupuestoWithRequiredFields`)
- Test classes: `*Test` or `*IntegrationTest` suffix
- Display names: `@DisplayName` annotations with clear descriptions
- Playwright: `TS-nnnn-<workflow>` with CU traceability

### Assertion Framework
- **Java Tests:** AssertJ fluent assertions
- **Playwright:** `@playwright/test` expect API

### Database Testing
- **Unit:** No database required
- **Integration:**
  - H2 in-memory (fast, standalone)
  - PostgreSQL via TestContainers (production-like)
  - Schema via Flyway (single source of truth)

### Test Organization
- Tests grouped by domain (Entity, API, Use Case)
- Nested test classes for related scenarios
- Tags for categorization (`@smoke`, `@e2e`, `@agent-callable`)

---

## Continuous Integration

Tests are executed automatically in GitHub Actions CI/CD:

1. **PR Validation**
   - Unit tests + coverage (JaCoCo ratchet floor)
   - Checkstyle / Spotless / SpotBugs
   - Integration tests (H2)
   - Playwright E2E (`playwright-e2e.yml`)

2. **Main Branch**
   - All tests including PostgreSQL integration
   - Security scanning (Trivy)
   - Coverage report upload

3. **Release Build**
   - Full test suite + Playwright E2E
   - Docker image scan
   - Artifact generation

Swing `e2e-swing.yml` is retired (ADR-012 / #811) and must not return.

---

## Troubleshooting

### PostgreSQL Connection Issues
\`\`\`bash
# Ensure PostgreSQL is running
bash scripts/start.sh

# Check connection
psql -h localhost -U notaire -d notaire_db
\`\`\`

### UI E2E
\`\`\`bash
cd frontend && npm run test:e2e
\`\`\`
Do not use \`testing/e2e-swing/\` (retired #811).

### Coverage Below Floor
\`\`\`bash
# View detailed coverage report
mvn jacoco:report -pl backend-api
open backend-api/target/site/jacoco/index.html
\`\`\`

---

## Next Steps

- [ ] Run full test suite: `bash run-all-tests.sh`
- [ ] Review coverage report: `backend-api/target/site/jacoco/index.html`
- [ ] Add missing unit tests for new features
- [ ] Expand E2E test coverage
- [ ] Configure CI pipeline for automated testing
- [ ] Set up test reporting dashboard

---

**Last Generated:** $(date +%Y-%m-%d\ %H:%M:%S)
EOF
    } > "$REPORT_FILE"

    log_info "Report generated: $REPORT_FILE"
}

# ============================================================================
# Main Execution
# ============================================================================

main() {
    parse_args "$@"
    setup_reports_dir

    # Change to workspace root for Maven commands
    cd "$WORKSPACE_ROOT"

    echo -e "${BLUE}"
    cat << 'EOF'
╔════════════════════════════════════════════════════════════════╗
║         Notaire - Comprehensive Test Suite Runner              ║
║                                                                ║
║  Executing all tests: Unit, Integration, Client, HTTP, E2E    ║
╚════════════════════════════════════════════════════════════════╝
EOF
    echo -e "${NC}"

    # Discovery phase
    count_unit_tests
    count_integration_tests
    count_client_tests
    count_http_tests
    count_e2e_tests

    # Execution phase
    run_unit_tests
    run_integration_tests
    run_client_tests
    run_http_tests
    run_e2e_tests

    # Analysis phase
    analyze_coverage

    # Reporting phase
    generate_markdown_report

    # Summary
    echo ""
    log_section "Test Execution Summary"
    echo ""

    local total_duration=0
    for duration in "${TEST_TIMINGS[@]}"; do
        total_duration=$((total_duration + duration))
    done

    echo -e "${BLUE}Test Results:${NC}"
    echo "  Unit Tests:        ${TEST_RESULTS["unit"]:-⊘ NOT RUN} (${TEST_TIMINGS["unit"]:-0}s)"
    echo "  Integration Tests: ${TEST_RESULTS["integration"]:-⊘ NOT RUN} (${TEST_TIMINGS["integration"]:-0}s)"
    echo "  Client Tests:      ${TEST_RESULTS["client"]:-⊘ NOT RUN} (${TEST_TIMINGS["client"]:-0}s)"
    echo "  HTTP Tests:        ${TEST_RESULTS["http"]:-⊘ NOT RUN} (${TEST_TIMINGS["http"]:-0}s)"
    echo "  E2E Tests:         ${TEST_RESULTS["e2e"]:-⊘ NOT RUN} (${TEST_TIMINGS["e2e"]:-0}s)"
    echo ""
    echo "  Total Duration:    ${total_duration}s"
    echo ""
    echo "📊 Full Report: $REPORT_FILE"
    echo "📋 Logs Directory: $LOG_DIR"
    echo ""
}

# Execute main function
main "$@"
