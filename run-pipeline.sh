#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# run-pipeline.sh — Full CI Pipeline Runner
# =============================================================================
# Runs the complete CI gate stack locally, mirroring GitHub Actions:
#   ci.yml (build, unit-tests, integration-tests, coverage, security, docker, quality)
#   pr-validation.yml (quick-build, dependency-analysis, lint, branch-naming, sdlc-plan)
#   frontend-ci.yml (frontend build, lint, typecheck, test)
#   playwright-e2e.yml (E2E tests)
#
# Usage:
#   bash run-pipeline.sh [OPTIONS]
#
# Options:
#   --fast              Skip Docker build and E2E tests
#   --skip-e2e          Skip Playwright E2E tests only
#   --skip-docker       Skip Docker build only
#   --skip-pg           Skip PostgreSQL integration tests (need Docker)
#   --fix               Auto-fix formatting issues (Spotless)
#   --backend-only      Run only backend checks
#   --frontend-only     Run only frontend checks
#   --ci                Strict mode: fail-fast on first error (default: continue)
#   -v, --verbose       Show Maven/Gradle output
#   -h, --help          Show this help
#
# Exit codes:
#   0   All checks passed
#   1   One or more checks failed (see summary)
# =============================================================================

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# ──────────────────────────────────────────────────────────────────────────────
# Parse arguments
# ──────────────────────────────────────────────────────────────────────────────
FAST=false
SKIP_E2E=false
SKIP_DOCKER=false
SKIP_PG=false
FIX=false
BACKEND_ONLY=false
FRONTEND_ONLY=false
CI_MODE=false
VERBOSE=false

while [[ $# -gt 0 ]]; do
  case $1 in
    --fast)         FAST=true; SKIP_E2E=true; SKIP_DOCKER=true; shift ;;
    --skip-e2e)     SKIP_E2E=true; shift ;;
    --skip-docker)  SKIP_DOCKER=true; shift ;;
    --skip-pg)      SKIP_PG=true; shift ;;
    --fix)          FIX=true; shift ;;
    --backend-only) BACKEND_ONLY=true; shift ;;
    --frontend-only) FRONTEND_ONLY=true; shift ;;
    --ci)           CI_MODE=true; shift ;;
    -v|--verbose)   VERBOSE=true; shift ;;
    -h|--help)      sed -n '2,/^# =====/p' "$0" | sed 's/^# //' | sed 's/^#//'; exit 0 ;;
    *)              echo "Unknown option: $1"; exit 1 ;;
  esac
done

# ──────────────────────────────────────────────────────────────────────────────
# Globals
# ──────────────────────────────────────────────────────────────────────────────
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
RESULTS_FILE=$(mktemp)
LOG_FILE="${PROJECT_ROOT}/pipeline_report.log"
FAIL_COUNT=0
PASS_COUNT=0
WARN_COUNT=0
START_TIME=$(date +%s)

MAVEN_OPTS="${MAVEN_OPTS:--Xmx1024m -XX:MaxMetaspaceSize=512m}"
export MAVEN_OPTS

# ──────────────────────────────────────────────────────────────────────────────
# Initialize log file
# ──────────────────────────────────────────────────────────────────────────────
init_log() {
  {
    echo "══════════════════════════════════════════════════════════════════════════════"
    echo "NOTAIRE Pipeline Report — $(date '+%Y-%m-%d %H:%M:%S')"
    echo "══════════════════════════════════════════════════════════════════════════════"
    echo ""
    echo "This report captures errors, warnings, and diagnostic traces."
    echo ""
  } > "$LOG_FILE"
}

# Write to log file only (not console)
log_to_file() {
  echo "$@" >> "$LOG_FILE"
}

# Write to both console and log file
log_dual() {
  echo "$@"
  echo "$@" >> "$LOG_FILE"
}

# ──────────────────────────────────────────────────────────────────────────────
# Helpers
# ──────────────────────────────────────────────────────────────────────────────
log_header() {
  echo ""
  echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo -e "${BOLD}${BLUE}  $1${NC}"
  echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

log_step() {
  echo -e "${CYAN}▸ $1${NC}"
}

log_pass() {
  PASS_COUNT=$((PASS_COUNT + 1))
  echo -e "${GREEN}  ✅ PASS${NC} — $1"
  echo "PASS: $1" >> "$RESULTS_FILE"
}

log_fail() {
  FAIL_COUNT=$((FAIL_COUNT + 1))
  echo -e "${RED}  ❌ FAIL${NC} — $1"
  echo "FAIL: $1" >> "$RESULTS_FILE"
  if [[ "$CI_MODE" == "true" ]]; then
    echo -e "${RED}${BOLD}  Failing fast (--ci mode)${NC}"
    print_summary
    exit 1
  fi
}

log_warn() {
  WARN_COUNT=$((WARN_COUNT + 1))
  echo -e "${YELLOW}  ⚠️  WARN${NC} — $1"
  echo "WARN: $1" >> "$RESULTS_FILE"
}

run_cmd() {
  local label="$1"; shift
  log_step "$label"
  if [[ "$VERBOSE" == "true" ]]; then
    if "$@"; then
      log_pass "$label"
      return 0
    else
      log_fail "$label"
      return 1
    fi
  else
    local output
    if output=$("$@" 2>&1); then
      log_pass "$label"
      return 0
    else
      log_fail "$label"
      echo "$output" | tail -20
      return 1
    fi
  fi
}

print_summary() {
  local end_time
  end_time=$(date +%s)
  local elapsed=$((end_time - START_TIME))
  local minutes=$((elapsed / 60))
  local seconds=$((elapsed % 60))

  echo ""
  echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo -e "${BOLD}  PIPELINE RESULTS${NC}"
  echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo ""
  echo -e "  ${GREEN}✅ Passed: $PASS_COUNT${NC}"
  echo -e "  ${RED}❌ Failed: $FAIL_COUNT${NC}"
  echo -e "  ${YELLOW}⚠️  Warned: $WARN_COUNT${NC}"
  echo -e "  ⏱  Duration: ${minutes}m ${seconds}s"
  echo ""

  if [[ $FAIL_COUNT -gt 0 ]]; then
    echo -e "${RED}${BOLD}  ❌ PIPELINE FAILED — $FAIL_COUNT check(s) failed${NC}"
    echo ""
    echo "Failed checks:"
    grep "^FAIL:" "$RESULTS_FILE" | sed 's/^FAIL:/  - /'
    echo ""
  else
    echo -e "${GREEN}${BOLD}  ✅ PIPELINE PASSED${NC}"
  fi
}

cleanup() {
  rm -f "$RESULTS_FILE"
}
trap 'print_summary; cleanup' EXIT

# ──────────────────────────────────────────────────────────────────────────────
# MAIN
# ──────────────────────────────────────────────────────────────────────────────
init_log
echo ""
echo -e "${BOLD}${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BOLD}${CYAN}║              NOTAIRE — Full CI Pipeline Runner                            ║${NC}"
echo -e "${BOLD}${CYAN}║              $(date '+%Y-%m-%d %H:%M:%S')                                        ║${NC}"
echo -e "${BOLD}${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"

cd "$PROJECT_ROOT"

# ══════════════════════════════════════════════════════════════════════════════
# PHASE 1: Backend Build & Compile
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$FRONTEND_ONLY" != "true" ]]; then
  log_header "PHASE 1 — Backend Build & Compile"

  run_cmd "Maven clean compile (skip tests)" \
    mvn clean compile -pl backend-api -am -DskipTests -q

  run_cmd "Validate project structure" bash -c \
    'mvn help:evaluate -Dexpression=project.modules -q -DforceStdout 2>/dev/null && \
     ls -la backend-api/target/classes >/dev/null 2>&1'
fi

# ══════════════════════════════════════════════════════════════════════════════
# PHASE 2: Backend Unit Tests + JaCoCo Coverage
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$FRONTEND_ONLY" != "true" ]]; then
  log_header "PHASE 2 — Backend Unit Tests (JaCoCo)"

  run_cmd "Unit tests (excl. integration/)" \
    mvn test -pl backend-api -am \
      -Dtest='!**/integration/**' \
      -Dsurefire.failIfNoSpecifiedTests=false
fi

# ══════════════════════════════════════════════════════════════════════════════
# PHASE 3: Backend Integration Tests (H2 + Testcontainers)
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$FRONTEND_ONLY" != "true" ]]; then
  log_header "PHASE 3 — Backend Integration Tests"

  run_cmd "Integration tests (H2)" \
    mvn test -pl backend-api -am \
      -Dtest='**/integration/**' \
      -Dsurefire.failIfNoSpecifiedTests=false

  if [[ "$SKIP_PG" != "true" ]]; then
    log_step "Checking Docker daemon for Testcontainers..."
    if docker info >/dev/null 2>&1; then
      run_cmd "Integration tests (Testcontainers / PostgreSQL)" \
        mvn test -pl backend-api -am -Ppg-integration
    else
      log_warn "Docker not running — skipping Testcontainers/PostgreSQL tests"
    fi
  else
    log_warn "Skipping Testcontainers/PostgreSQL tests (--skip-pg)"
  fi
fi

# ══════════════════════════════════════════════════════════════════════════════
# PHASE 4: Coverage Gate — mvn verify (JaCoCo ratchet floor)
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$FRONTEND_ONLY" != "true" ]]; then
  log_header "PHASE 4 — Coverage Gate (mvn verify)"

  run_cmd "Full verify (tests + coverage gate + checkstyle)" \
    mvn verify -pl backend-api -am \
      -Dsurefire.failIfNoSpecifiedTests=false

  # ── JaCoCo coverage summary ──────────────────────────────────────────────
  JACOCO_CSV="backend-api/target/site/jacoco/jacoco.csv"
  if [[ -f "$JACOCO_CSV" ]]; then
    # CSV header: GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,...
    # Aggregate all rows (skip header) into totals
    awk -F',' 'NR>1 {
      im+=$4; ic+=$5; bm+=$6; bc+=$7; mm+=$8; mc+=$9; tm+=$10; tc+=$11
    } END {
      itotal=im+ic; btotal=bm+bc; ttotal=tm+tc
      ipct=(itotal>0)?(ic*100/itotal):0
      bpct=(btotal>0)?(bc*100/btotal):0
      tpct=(ttotal>0)?(tc*100/ttotal):0
      printf "%.1f%% lines | %.1f%% branches | %.1f%% instructions\n", tpct, bpct, ipct
    }' "$JACOCO_CSV"
    log_pass "Backend JaCoCo coverage report: backend-api/target/site/jacoco/index.html"
  elif [[ -f backend-api/target/site/jacoco/index.html ]]; then
    log_pass "Coverage report available at backend-api/target/site/jacoco/index.html"
  fi
fi

# ══════════════════════════════════════════════════════════════════════════════
# PHASE 5: Lint & Format (Spotless, Checkstyle, SpotBugs)
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$FRONTEND_ONLY" != "true" ]]; then
  log_header "PHASE 5 — Lint & Format"

  # Spotless format check (ratchetFrom = origin/main scoped to changed files)
  if [[ "$FIX" == "true" ]]; then
    log_step "Auto-fixing formatting (Spotless)..."
    mvn spotless:apply -pl backend-api -q 2>/dev/null || true
    log_pass "Spotless auto-fix applied"
  fi

  run_cmd "Spotless format check" \
    mvn spotless:check -pl backend-api -q

  run_cmd "Checkstyle (advisory — #705)" \
    mvn checkstyle:check -pl backend-api -q || true

  run_cmd "SpotBugs (advisory)" \
    mvn compile com.github.spotbugs:spotbugs-maven-plugin:spotbugs \
      -pl backend-api -am -DskipSpotBugs=false -q || true

  if [[ -f backend-api/target/spotbugsXml.xml ]]; then
    bugs=$(grep -c "<BugInstance" backend-api/target/spotbugsXml.xml 2>/dev/null || echo "0")
    if [[ "$bugs" -gt 0 ]]; then
      log_warn "SpotBugs found $bugs finding(s) — see backend-api/target/spotbugsXml.xml"
    else
      log_pass "SpotBugs: no findings"
    fi
  fi
fi

# ══════════════════════════════════════════════════════════════════════════════
# PHASE 6: SDLC Plan Validation
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$FRONTEND_ONLY" != "true" ]]; then
  log_header "PHASE 6 — SDLC Plan Validation"

  if [[ -f scripts/validate-sdlc-plan.sh ]]; then
    run_cmd "Validate SDLC plan (Constitution check)" \
      bash scripts/validate-sdlc-plan.sh || true
  else
    log_warn "scripts/validate-sdlc-plan.sh not found — skipping"
  fi
fi

# ══════════════════════════════════════════════════════════════════════════════
# PHASE 7: Frontend Build & Lint
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$BACKEND_ONLY" != "true" ]] && [[ -d frontend ]]; then
  log_header "PHASE 7 — Frontend Build & Lint"

  (cd frontend && npm ci --no-audit --no-fund 2>&1 | tail -1) || true

  run_cmd "Frontend build" \
    bash -c 'cd frontend && npm run build'

  run_cmd "Frontend lint (eslint --max-warnings=0)" \
    bash -c 'cd frontend && npm run lint'

  run_cmd "Frontend typecheck (tsc --noEmit)" \
    bash -c 'cd frontend && npm run typecheck'

  run_cmd "Frontend unit tests (vitest --coverage)" \
    bash -c 'cd frontend && npm run test:coverage'

  # ── Vitest coverage summary ─────────────────────────────────────────────
  FE_COV="frontend/coverage/coverage-summary.json"
  if [[ -f "$FE_COV" ]] && command -v python3 >/dev/null 2>&1; then
    python3 -c "
import json, sys
with open('$FE_COV') as f:
    t = json.load(f)['total']
print(
    f\"{t['lines']['pct']:.1f}% lines | \"
    f\"{t['branches']['pct']:.1f}% branches | \"
    f\"{t['functions']['pct']:.1f}% functions\"
)
"
    log_pass "Frontend coverage report: frontend/coverage/index.html"
  elif [[ -f frontend/coverage/index.html ]]; then
    log_pass "Frontend coverage report available at frontend/coverage/index.html"
  fi
fi

# ══════════════════════════════════════════════════════════════════════════════
# PHASE 8: Docker Build
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$SKIP_DOCKER" != "true" ]] && [[ "$FRONTEND_ONLY" != "true" ]]; then
  log_header "PHASE 8 — Docker Build"

  if docker info >/dev/null 2>&1; then
    run_cmd "Docker compose build" \
      docker compose build --no-cache

    run_cmd "Docker compose up (smoke test)" bash -c \
      'docker compose up -d && sleep 10 && docker compose ps && docker compose down'
  else
    log_warn "Docker not running — skipping Docker build"
  fi
else
  log_warn "Skipping Docker build (--skip-docker or --frontend-only)"
fi

# ══════════════════════════════════════════════════════════════════════════════
# PHASE 9: Playwright E2E Tests
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$SKIP_E2E" != "true" ]] && [[ -d frontend ]]; then
  log_header "PHASE 9 — Playwright E2E Tests"

  # Check if backend + frontend are running (needed for E2E)
  if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1 || \
     curl -sf http://localhost:3000 >/dev/null 2>&1; then
    run_cmd "Playwright E2E tests" \
      bash -c 'cd frontend && npx playwright test'
  else
    log_warn "Backend/frontend not running on localhost — skipping Playwright E2E"
    log_warn "Start with: bash scripts/start.sh && cd frontend && npm run dev"
  fi
else
  log_warn "Skipping Playwright E2E (--skip-e2e or --frontend-only)"
fi

# ══════════════════════════════════════════════════════════════════════════════
# PHASE 10: Bruno API Integration Tests
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$BACKEND_ONLY" != "true" ]] && [[ "$FAST" != "true" ]]; then
  log_header "PHASE 10 — Bruno API Integration Tests"

  if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then
    if [[ -d integration-test ]]; then
      run_cmd "Bruno API tests" \
        bash integration-test/scripts/test.sh || true
    else
      log_warn "integration-test/ directory not found — skipping"
    fi
  else
    log_warn "Backend not running on :8080 — skipping Bruno API tests"
    log_warn "Start with: bash scripts/start.sh"
  fi
fi

# ══════════════════════════════════════════════════════════════════════════════
# PHASE 11: Security Scan (Trivy — advisory)
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$FRONTEND_ONLY" != "true" ]] && [[ "$FAST" != "true" ]]; then
  log_header "PHASE 11 — Security Scan (Trivy)"

  if command -v trivy >/dev/null 2>&1; then
    run_cmd "Trivy filesystem scan" \
      trivy fs . --exit-code 0 --ignore-unfixed --quiet || true
  else
    log_warn "trivy not installed — skipping security scan"
    log_warn "Install: brew install trivy"
  fi
fi

# ══════════════════════════════════════════════════════════════════════════════
# DONE
# ══════════════════════════════════════════════════════════════════════════════
exit "$FAIL_COUNT"
