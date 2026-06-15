#!/bin/bash

# Comprehensive Test Execution Script for Notaire Project
# Runs all test phases and generates unified coverage reports
# Requires: Maven, Docker (for integration tests), Node.js (for E2E tests)

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

echo "=========================================="
echo "Notaire Comprehensive Test Suite"
echo "=========================================="
echo "Project Root: $PROJECT_ROOT"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print section headers
print_header() {
    echo ""
    echo -e "${YELLOW}========== $1 ==========${NC}"
    echo ""
}

# Function to print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to print error
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Track test results
PHASE_RESULTS=""
TOTAL_TESTS=0
FAILED_TESTS=0

# Phase 1: Unit Tests
print_header "PHASE 1: Unit Tests (Backend)"
cd "$PROJECT_ROOT/backend-api"

echo "Running unit tests..."
if mvn test -q -DskipTests=false -Dtest="**/unit/*" 2>/dev/null; then
    print_success "Unit tests passed"
    PHASE_RESULTS="${PHASE_RESULTS}✓ Phase 1: Unit Tests\n"
else
    print_error "Unit tests failed"
    PHASE_RESULTS="${PHASE_RESULTS}✗ Phase 1: Unit Tests\n"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Phase 2: Integration Tests (Repository Layer)
print_header "PHASE 2: Repository Integration Tests"

echo "Running repository integration tests..."
if mvn test -q -DskipTests=false -Dtest="**/integration/*Repository*" 2>/dev/null; then
    print_success "Repository integration tests passed"
    PHASE_RESULTS="${PHASE_RESULTS}✓ Phase 2: Repository Integration Tests\n"
else
    print_error "Repository integration tests failed"
    PHASE_RESULTS="${PHASE_RESULTS}✗ Phase 2: Repository Integration Tests\n"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Phase 3: Service Integration Tests
print_header "PHASE 3: Service Integration Tests"

echo "Running service integration tests..."
if mvn test -q -DskipTests=false -Dtest="**/integration/*Service*" 2>/dev/null; then
    print_success "Service integration tests passed"
    PHASE_RESULTS="${PHASE_RESULTS}✓ Phase 3: Service Integration Tests\n"
else
    print_error "Service integration tests failed"
    PHASE_RESULTS="${PHASE_RESULTS}✗ Phase 3: Service Integration Tests\n"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Phase 4: API Testing with Bruno
print_header "PHASE 4: API Integration Testing (Bruno CLI)"

cd "$PROJECT_ROOT/backend-api/api-test"

if command -v bru &> /dev/null; then
    echo "Bruno CLI found: $(bru --version)"

    # Check if application is running
    if curl -s http://localhost:8080/swagger-ui.html > /dev/null 2>&1; then
        echo "Backend API is running, executing Bruno tests..."
        if bru run . -r --env Development --reporter-cli 2>/dev/null; then
            print_success "API tests passed"
            PHASE_RESULTS="${PHASE_RESULTS}✓ Phase 4: API Testing\n"
        else
            print_error "API tests failed"
            PHASE_RESULTS="${PHASE_RESULTS}✗ Phase 4: API Testing\n"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        echo "⚠ Backend API not running on http://localhost:8080"
        echo "  Start with: bash scripts/start.sh"
        PHASE_RESULTS="${PHASE_RESULTS}⊘ Phase 4: API Testing (Skipped - API not running)\n"
    fi
else
    echo "⚠ Bruno CLI not found. Install with: brew install bruno"
    PHASE_RESULTS="${PHASE_RESULTS}⊘ Phase 4: API Testing (Skipped - Bruno not installed)\n"
fi

# Phase 5: E2E Testing with Playwright
print_header "PHASE 5: E2E UI Testing (Playwright)"

cd "$PROJECT_ROOT/frontend"

if [ -f "package.json" ]; then
    if command -v npx &> /dev/null; then
        echo "Running Playwright tests..."
        if npx playwright test --reporter=html 2>/dev/null; then
            print_success "E2E tests passed"
            PHASE_RESULTS="${PHASE_RESULTS}✓ Phase 5: E2E Testing\n"
            echo "  Report: ./playwright-report/index.html"
        else
            print_error "E2E tests failed"
            PHASE_RESULTS="${PHASE_RESULTS}✗ Phase 5: E2E Testing\n"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        echo "⚠ Node.js/npm not found"
        PHASE_RESULTS="${PHASE_RESULTS}⊘ Phase 5: E2E Testing (Skipped - Node.js not installed)\n"
    fi
else
    echo "⚠ Frontend directory not found or not a Node.js project"
    PHASE_RESULTS="${PHASE_RESULTS}⊘ Phase 5: E2E Testing (Skipped - Frontend not configured)\n"
fi

# Phase 6: Coverage Analysis
print_header "PHASE 6: Code Coverage Analysis"

cd "$PROJECT_ROOT/backend-api"

echo "Generating JaCoCo coverage report..."
mvn jacoco:report -q 2>/dev/null

if [ -f "target/site/jacoco/index.html" ]; then
    print_success "Coverage report generated"
    echo "  Report: $PROJECT_ROOT/backend-api/target/site/jacoco/index.html"
    PHASE_RESULTS="${PHASE_RESULTS}✓ Phase 6: Coverage Analysis\n"
else
    print_error "Failed to generate coverage report"
    PHASE_RESULTS="${PHASE_RESULTS}✗ Phase 6: Coverage Analysis\n"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Summary
print_header "Test Execution Summary"

echo -e "$PHASE_RESULTS"

# Get actual test count
cd "$PROJECT_ROOT/backend-api"
TEST_COUNT=$(mvn test -q 2>&1 | grep "Tests run:" | tail -1 | grep -oP '\d+(?= tests)' || echo "Unknown")

echo "Total Tests Executed: $TEST_COUNT"
echo "Failed Phases: $FAILED_TESTS"

if [ $FAILED_TESTS -eq 0 ]; then
    print_success "All test phases completed successfully!"
    exit 0
else
    print_error "$FAILED_TESTS test phase(s) failed or were skipped"
    exit 1
fi
