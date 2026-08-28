#!/usr/bin/env bash
#
# preflight.sh — run the same gates CI runs, locally, before pushing.
#
# WHY
# ---
# CI spreads its gates across four workflows (ci.yml, pr-validation.yml,
# frontend-ci.yml, playwright-e2e.yml). Some of those gates are NOT reachable
# through the commands developers normally run: `mvn verify` does not invoke
# Spotless (it is deliberately unbound in backend-api/pom.xml, see #705), so a
# branch can be fully green locally and still fail "Code Lint" in CI. This script
# is the single local entry point that covers every blocking gate, so a push that
# passes here passes CI.
#
# KEEPING IT HONEST
# -----------------
# Each check below records the CI job it mirrors. If you add or change a gate in
# .github/workflows/, update the matching check here in the same PR — otherwise
# the local/CI gap reopens. `--list` prints the mapping.
#
# USAGE
#   bash scripts/preflight.sh            # everything except server-backed suites
#   bash scripts/preflight.sh --fix      # auto-fix what is fixable, then verify
#   bash scripts/preflight.sh --fast     # format/lint/compile/typecheck only
#   bash scripts/preflight.sh --full     # adds Playwright E2E + Bruno API tests + Docker build/smoke test
#   bash scripts/preflight.sh --list     # show local-check -> CI-job mapping
#
# Exit code is non-zero if any blocking check fails.
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

MODE_FIX=0; MODE_FAST=0; MODE_FULL=0
for arg in "$@"; do
    case "$arg" in
        --fix)  MODE_FIX=1 ;;
        --fast) MODE_FAST=1 ;;
        --full) MODE_FULL=1 ;;
        --list)
            cat <<'MAP'
Local check                     CI job                          Workflow
------------------------------  ------------------------------  --------------------
branch naming                   Branch Naming Convention Check   pr-validation.yml  (warn)
sdlc plan (openspec changes)    SDLC Plan Validation             pr-validation.yml  (BLOCKING)
sdlc plan (speckit features)    SpecKit Plan Validation           pr-validation.yml  (BLOCKING)
spotless format                 Code Lint / Format Check         pr-validation.yml  (BLOCKING)
checkstyle                      Code Lint / Checkstyle           pr-validation.yml  (warn, CI uses || true)
dependency analysis             Dependency Analysis              pr-validation.yml  (warn, CI uses || true)
spotbugs                        Code Quality / SpotBugs          ci.yml             (warn, report-only)
trivy filesystem scan           Security Scan                    ci.yml             (warn, report-only)
backend compile                 Build & Compile / Quick Build    ci.yml, pr-validation.yml
backend unit+integration+cov    Unit Tests, Integration Tests,   ci.yml
                                Coverage Gate (mvn verify)
backend pg-integration          Integration Tests (Testcontainers) ci.yml
frontend typecheck              TypeScript Check                 frontend-ci.yml
frontend eslint                 ESLint                           frontend-ci.yml
frontend vitest                 Unit Tests (Vitest)              frontend-ci.yml
frontend build                  Build (Next.js)                  frontend-ci.yml
http integration suite (--full) (legacy cURL smoke; no CI job)   n/a
playwright e2e          (--full) UI E2E Tests (Playwright)       playwright-e2e.yml
bruno api tests         (--full) API Tests (Bruno)               playwright-e2e.yml
docker build/smoke      (--full) Build Docker Image              ci.yml             (image scan not replicated locally)
MAP
            exit 0 ;;
        -h|--help) sed -n '3,28p' "${BASH_SOURCE[0]}"; exit 0 ;;
        *) echo "unknown option: $arg (try --help)"; exit 2 ;;
    esac
done

RED=$'\033[0;31m'; GREEN=$'\033[0;32m'; YELLOW=$'\033[1;33m'; BLUE=$'\033[0;34m'; NC=$'\033[0m'
FAILED=(); WARNED=(); PASSED=(); SKIPPED=()

section() { printf '\n%s=== %s ===%s\n' "$BLUE" "$1" "$NC"; }
pass()    { PASSED+=("$1");  printf '%s✓%s %s\n' "$GREEN" "$NC" "$1"; }
warn()    { WARNED+=("$1");  printf '%s!%s %s\n' "$YELLOW" "$NC" "$1"; }
skip()    { SKIPPED+=("$1"); printf '%s-%s %s (skipped: %s)\n' "$YELLOW" "$NC" "$1" "$2"; }
fail()    { FAILED+=("$1");  printf '%s✗%s %s\n' "$RED" "$NC" "$1"; }

# run <label> <cmd...> — blocking check
run() {
    local label="$1"; shift
    local log; log="$(mktemp)"
    if "$@" >"$log" 2>&1; then
        pass "$label"
    else
        fail "$label"
        printf '%s--- last 25 lines: %s ---%s\n' "$RED" "$label" "$NC"
        tail -25 "$log"
        printf '%s--- full log: %s ---%s\n' "$RED" "$log" "$NC"
        return 1
    fi
    rm -f "$log"
}

# run_warn <label> <cmd...> — non-blocking (mirrors CI steps that use `|| true`)
run_warn() {
    local label="$1"; shift
    local log; log="$(mktemp)"
    if "$@" >"$log" 2>&1; then pass "$label"; else warn "$label (non-blocking in CI)"; fi
    rm -f "$log"
}

MVN_FLAGS=(--no-transfer-progress -q)

# ---------------------------------------------------------------------------
section "Repo state"
# ---------------------------------------------------------------------------
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
echo "branch: $BRANCH"
# Spotless ratchets against origin/main; without it the format gate cannot run.
git fetch origin main --quiet 2>/dev/null || warn "could not fetch origin/main (offline?)"

# Mirrors: Branch Naming Convention Check (pr-validation.yml) — warn-only there too.
if [[ "$BRANCH" =~ ^(main|develop|feature/|bugfix/|hotfix/|release/|[0-9]+/(feat|fix|docs|refactor|ci|chore|perf|test|build|revert)/) ]] \
   || [[ "$BRANCH" =~ ^(feat|fix|docs|refactor|ci|chore|perf|test|build|revert|design|add)/[0-9]+_ ]]; then
    pass "branch naming"
else
    warn "branch naming: '$BRANCH' does not match a documented pattern"
fi

# Engineering Constitution: every active OpenSpec change must carry a complete
# SDLC plan (CONSTITUTION.md §5, §6). Cheap and fails fast, so it runs first.
run "sdlc plan validation" bash scripts/validate-sdlc-plan.sh

# Same gate, for the SpecKit-adapted flow under speckit/ (evaluated alongside
# OpenSpec — see speckit/NOTAIRE-ADAPTATIONS.md).
run "speckit plan validation" bash scripts/validate-speckit-plan.sh

# ---------------------------------------------------------------------------
section "Format & lint (BLOCKING in CI: 'Code Lint')"
# ---------------------------------------------------------------------------
# Spotless resolves the repo via JGit, which cannot read a git worktree's `.git`
# file. Use the plugin where it works, the equivalent fallback where it doesn't.
GIT_DIR_PATH="$(git rev-parse --git-dir)"
SPOTLESS_NATIVE=1
[ -f "$REPO_ROOT/.git" ] && SPOTLESS_NATIVE=0   # .git is a file => worktree

if [ "$MODE_FIX" = "1" ]; then
    if [ "$SPOTLESS_NATIVE" = "1" ]; then
        run_warn "spotless:apply" mvn spotless:apply -pl backend-api "${MVN_FLAGS[@]}"
    else
        run_warn "spotless:apply (fallback: git worktree)" bash scripts/spotless-fallback.sh apply
    fi
fi

if [ "$SPOTLESS_NATIVE" = "1" ]; then
    run "spotless format check" mvn spotless:check -pl backend-api "${MVN_FLAGS[@]}"
else
    run "spotless format check (fallback: git worktree)" bash scripts/spotless-fallback.sh check
fi

run_warn "checkstyle" mvn checkstyle:check -pl backend-api "${MVN_FLAGS[@]}"

# ---------------------------------------------------------------------------
section "Report-only checks (mirror ci.yml/pr-validation.yml advisory jobs)"
# ---------------------------------------------------------------------------
if [ "$MODE_FAST" = "0" ]; then
    run_warn "dependency analysis" mvn dependency:analyze -pl backend-api -am "${MVN_FLAGS[@]}"
    run_warn "spotbugs" mvn compile com.github.spotbugs:spotbugs-maven-plugin:spotbugs -pl backend-api -am -DskipSpotBugs=false "${MVN_FLAGS[@]}"
    if command -v trivy >/dev/null 2>&1; then
        run_warn "trivy filesystem scan" trivy fs . --exit-code 0 --ignore-unfixed --quiet
    else
        skip "trivy filesystem scan" "trivy not installed — CI WILL run this"
    fi
else
    skip "dependency analysis / spotbugs / trivy fs" "--fast"
fi

# ---------------------------------------------------------------------------
section "Backend"
# ---------------------------------------------------------------------------
run "backend compile" mvn clean compile -DskipTests "${MVN_FLAGS[@]}"

if [ "$MODE_FAST" = "0" ]; then
    # `mvn verify` covers Unit Tests + Integration Tests (H2) + Coverage Gate.
    run "backend verify (tests + coverage gate)" mvn verify -pl backend-api -am "${MVN_FLAGS[@]}"

    # Testcontainers-backed; needs a working Docker daemon.
    if docker info >/dev/null 2>&1; then
        run "backend pg-integration tests" mvn test -pl backend-api -am -Ppg-integration "${MVN_FLAGS[@]}"
    else
        skip "backend pg-integration tests" "docker not running — CI WILL run these"
    fi
else
    skip "backend tests" "--fast"
fi

# ---------------------------------------------------------------------------
section "Frontend"
# ---------------------------------------------------------------------------
if [ ! -d frontend/node_modules ]; then
    echo "installing frontend deps (npm ci)…"
    (cd frontend && npm ci) >/dev/null 2>&1 || warn "npm ci failed"
fi

# Call the local binaries directly rather than `npm run …`: a shell proxy on this
# machine can rewrite npm scripts and widen ESLint's scope, which would make the
# local result diverge from CI's.
run "frontend typecheck" bash -c "cd frontend && ./node_modules/.bin/tsc --noEmit"

if [ "$MODE_FIX" = "1" ]; then
    run_warn "eslint --fix" bash -c "cd frontend && ./node_modules/.bin/eslint src --fix"
fi
run "frontend eslint" bash -c "cd frontend && ./node_modules/.bin/eslint src --max-warnings=0"

if [ "$MODE_FAST" = "0" ]; then
    run "frontend vitest" bash -c "cd frontend && ./node_modules/.bin/vitest run"
    run "frontend build" bash -c "cd frontend && ./node_modules/.bin/next build"
else
    skip "frontend tests + build" "--fast"
fi

# ---------------------------------------------------------------------------
section "Server-backed suites"
# ---------------------------------------------------------------------------
if [ "$MODE_FULL" = "1" ]; then
    if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1 \
       && curl -sf -o /dev/null http://localhost:3000 2>/dev/null; then
        run "http integration suite" bash -c "cd testing/http && bash test-all-endpoints-v2.sh"
        run "playwright e2e" bash -c "cd frontend && ./node_modules/.bin/playwright test"

        # Mirrors playwright-e2e.yml's "API Tests (Bruno)" job: fetch a JWT up
        # front (the collection sends it on every request; folders run
        # alphabetically so it can't be captured mid-run) and run the real
        # Bruno collection, not the legacy testing/http suite above.
        BRUNO_TOKEN="$(curl -sf -X POST http://localhost:8080/api/v1/usuarios/login \
            -H 'Content-Type: application/json' \
            -d '{"nombre":"admin","contrasenia":"admin"}' | tr -d '\n' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
        if [ -n "$BRUNO_TOKEN" ]; then
            run "bruno api tests" bash -c "cd backend-api/api-test && npx @usebruno/cli run --env Developmen --env-var token=$BRUNO_TOKEN"
        else
            fail "bruno api tests: could not obtain JWT token from login endpoint"
        fi
    else
        fail "server-backed suites: stack not reachable (need backend :8080 + frontend :3000 — run 'bash scripts/start.sh')"
    fi

    if docker info >/dev/null 2>&1; then
        run "docker compose build" docker compose build --no-cache
        run "docker compose smoke test" bash -c \
            'docker compose up -d && sleep 10 && docker compose ps && docker compose down'
    else
        skip "docker build/smoke test" "docker not running — CI WILL run this (docker-build job)"
    fi
else
    skip "playwright e2e / bruno api / docker build" "not --full; CI WILL run these"
fi

# ---------------------------------------------------------------------------
printf '\n%s================ PREFLIGHT SUMMARY ================%s\n' "$BLUE" "$NC"
printf '%spassed:%s  %d\n' "$GREEN" "$NC" "${#PASSED[@]}"
[ "${#WARNED[@]}"  -gt 0 ] && { printf '%swarn:%s    %d\n' "$YELLOW" "$NC" "${#WARNED[@]}"; printf '  ! %s\n' "${WARNED[@]}"; }
[ "${#SKIPPED[@]}" -gt 0 ] && { printf '%sskipped:%s %d\n' "$YELLOW" "$NC" "${#SKIPPED[@]}"; printf '  - %s\n' "${SKIPPED[@]}"; }

if [ "${#FAILED[@]}" -gt 0 ]; then
    printf '%sfailed:%s  %d\n' "$RED" "$NC" "${#FAILED[@]}"
    printf '  ✗ %s\n' "${FAILED[@]}"
    printf '\n%sPREFLIGHT FAILED — these would fail in CI.%s\n' "$RED" "$NC"
    printf 'Try: bash scripts/preflight.sh --fix\n'
    exit 1
fi

printf '\n%sPREFLIGHT PASSED%s\n' "$GREEN" "$NC"
if [ "$MODE_FAST" = "1" ] || [ "$MODE_FULL" = "0" ]; then
    printf '%sNote:%s some CI gates were skipped above — see the skipped list.\n' "$YELLOW" "$NC"
fi
exit 0
