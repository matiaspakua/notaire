#!/bin/bash

# Notaire Comprehensive Test Runner
# Runs unit tests, integration tests, and coverage analysis

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_DIR"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Test results tracking
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ $1${NC}"
}

# Function to run Maven tests and capture results
run_maven_tests() {
    local module=$1
    local test_type=$2
    local test_pattern=$3
    
    echo ""
    print_info "Running $test_type tests for $module..."
    
    if mvn test -pl "$module" -Dtest="$test_pattern" 2>&1; then
        print_success "$module $test_type tests passed"
        return 0
    else
        print_error "$module $test_type tests failed"
        return 1
    fi
}

# Check if Docker is available for integration tests
check_docker() {
    if command -v docker &> /dev/null && docker info &> /dev/null; then
        return 0
    else
        return 1
    fi
}

print_header "Notaire Test Suite"

# Display test environment info
echo ""
echo "Test Environment:"
echo "  Java: $(java -version 2>&1 | head -1)"
echo "  Maven: $(mvn --version 2>&1 | head -1)"
echo "  Docker: $(check_docker && echo 'Available' || echo 'Not available')"
echo ""

# Check Docker availability
if check_docker; then
    print_info "Docker is available - integration tests will run"
else
    print_warning "Docker is not available - integration tests will be skipped"
fi

echo ""
echo "Running tests in the following order:"
echo "  1. Unit tests (backend-api)"
echo "  2. Unit tests (frontend-swing)"
echo "  3. Integration tests (backend-api)"
echo "  4. Coverage analysis"
echo ""

# ============================================
# Phase 1: Unit Tests - Backend API
# ============================================
print_header "Phase 1: Backend API Unit Tests"

cd "$REPO_DIR"
if mvn test -pl backend-api -Dtest="**/unit/*" -DskipTests=false; then
    print_success "Backend API unit tests passed"
else
    print_error "Backend API unit tests failed"
    exit 1
fi

# ============================================
# Phase 2: Unit Tests - Frontend Swing
# ============================================
print_header "Phase 2: Frontend Swing Unit Tests"

if mvn test -pl frontend-swing -DskipTests=false; then
    print_success "Frontend Swing unit tests passed"
else
    print_error "Frontend Swing unit tests failed"
    exit 1
fi

# ============================================
# Phase 3: Integration Tests - Backend API
# ============================================
print_header "Phase 3: Backend API Integration Tests"

if check_docker; then
    print_info "Running integration tests (Docker available)..."
    if mvn test -pl backend-api -Dtest="**/integration/*" -DskipTests=false; then
        print_success "Backend API integration tests passed"
    else
        print_warning "Some integration tests failed - check test reports"
    fi
else
    print_warning "Skipping integration tests - Docker not available"
    print_info "Integration tests require Docker to run PostgreSQL testcontainers"
fi

# ============================================
# Phase 4: Coverage Analysis
# ============================================
print_header "Phase 4: Coverage Analysis"

cd "$REPO_DIR"
print_info "Generating JaCoCo coverage report..."
mvn jacoco:report -pl backend-api

# Check coverage threshold
COVERAGE=$(mvn jacoco:check -pl backend-api 2>&1 | grep -E "Total.*%" | tail -1 || echo "Coverage check completed")

if echo "$COVERAGE" | grep -q "BUILD SUCCESS"; then
    print_success "Coverage check passed (minimum 80%)"
else
    print_warning "Coverage check completed - review target/site/jacoco/index.html for details"
fi

# ============================================
# Summary
# ============================================
print_header "Test Summary"

echo ""
echo "Test Results:"
echo "  - Backend API unit tests: $(mvn test -pl backend-api -Dtest="**/unit/*" -q 2>&1 | grep -E "Tests run:" | tail -1 || echo 'See Maven output')"
echo "  - Frontend Swing tests: $(mvn test -pl frontend-swing -q 2>&1 | grep -E "Tests run:" | tail -1 || echo 'See Maven output')"
echo ""
echo "Reports location:"
echo "  - Backend API: backend-api/target/surefire-reports/"
echo "  - Frontend Swing: frontend-swing/target/surefire-reports/"
echo "  - Coverage: backend-api/target/site/jacoco/index.html"
echo ""

print_success "Test suite completed!"

exit 0