#!/usr/bin/env bash
#
# run_pipeline.sh — the mandatory, dashboarded pre-PR gate.
#
# WHY
# ---
# CONSTITUTION.md Gate 4 requires CI-equivalent evidence before a PR is ready
# for review, and .claude/rules/ai-agent-workflow.md Step 5 requires the full
# suite (unit + integration + E2E) to pass before committing. scripts/preflight.sh
# already mirrors every CI gate, but it assumes the Docker stack is already
# running for its server-backed suites and it only prints to the terminal —
# there is no single, reviewable artifact proving a branch is ready. This
# script is that single command: it brings the stack up itself, runs every
# gate, adds the one gate that exists nowhere else in the repo yet
# (markdown-lint), and writes one HTML dashboard + one log per run.
#
# COMPOSITION, NOT DUPLICATION
# -----------------------------
# This script does not reimplement any check — it calls:
#   - scripts/validate-sdlc-plan.sh
#   - scripts/start.sh          (idempotent; blocks until backend+frontend healthy)
#   - scripts/preflight.sh --full
# Add or change a gate in preflight.sh or .github/workflows/ and this script
# picks it up automatically; nothing to update here.
#
# GATES                            CI job
# --------------------------------  ------------------------------------------
# sdlc plan validation             pr-validation.yml (via preflight.sh)
# preflight --full (everything     ci.yml, pr-validation.yml, frontend-ci.yml,
#   preflight.sh --list covers)      playwright-e2e.yml (via preflight.sh)
# markdown-lint (changed *.md      CI job: none — local-only for now, see
#   files only, ratchet vs main)     issue #856. Same ratchet policy as the
#                                     backend's Spotless gate (issue #705):
#                                     only files this branch touches are
#                                     linted, so pre-existing docs are unaffected.
#
# USAGE
#   bash scripts/run_pipeline.sh
#
# Dashboard + logs are written under reports/pipeline/<timestamp>/ (git-ignored).
# Exit code is non-zero if any blocking gate fails.
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT" || exit 1

for arg in "$@"; do
    case "$arg" in
        -h|--help) sed -n '3,39p' "${BASH_SOURCE[0]}"; exit 0 ;;
        *) echo "unknown option: $arg (try --help)"; exit 2 ;;
    esac
done

RED=$'\033[0;31m'; GREEN=$'\033[0;32m'; YELLOW=$'\033[1;33m'; BLUE=$'\033[0;34m'; NC=$'\033[0m'

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
REPORT_DIR="$REPO_ROOT/reports/pipeline/$TIMESTAMP"
mkdir -p "$REPORT_DIR"
SUMMARY_LOG="$REPORT_DIR/pipeline.log"

PHASE_NAMES=(); PHASE_STATUSES=(); PHASE_LOGS=(); PHASE_DURATIONS=()

log() { printf '%s\n' "$*" | tee -a "$SUMMARY_LOG"; }

section() { log ""; log "=== $1 ==="; }

# phase <name> <cmd...> — blocking check; records status + log for the dashboard.
phase() {
    local name="$1"; shift
    local logfile start end duration
    logfile="$REPORT_DIR/$(echo "$name" | tr ' /' '--').log"
    start="$(date +%s)"
    section "$name"
    if "$@" >"$logfile" 2>&1; then
        end="$(date +%s)"; duration=$((end - start))
        log "${GREEN}✓${NC} $name (${duration}s)"
        PHASE_NAMES+=("$name"); PHASE_STATUSES+=("PASS"); PHASE_LOGS+=("$logfile"); PHASE_DURATIONS+=("$duration")
        return 0
    else
        end="$(date +%s)"; duration=$((end - start))
        log "${RED}✗${NC} $name (${duration}s) — see $logfile"
        tail -30 "$logfile" | tee -a "$SUMMARY_LOG"
        PHASE_NAMES+=("$name"); PHASE_STATUSES+=("FAIL"); PHASE_LOGS+=("$logfile"); PHASE_DURATIONS+=("$duration")
        return 1
    fi
}

# phase_skip <name> <reason> — records a skipped (non-blocking) phase for the dashboard.
phase_skip() {
    local name="$1" reason="$2"
    section "$name"
    log "${YELLOW}-${NC} $name (skipped: $reason)"
    PHASE_NAMES+=("$name"); PHASE_STATUSES+=("SKIP"); PHASE_LOGS+=(""); PHASE_DURATIONS+=("0")
}

PIPELINE_START="$(date +%s)"
OVERALL_FAILED=0

# -----------------------------------------------------------------------------
# 1. SDLC plan validation (OpenSpec Gate 1-3 mechanical check)
# -----------------------------------------------------------------------------
phase "sdlc plan validation" bash scripts/validate-sdlc-plan.sh || OVERALL_FAILED=1

# -----------------------------------------------------------------------------
# 2. Bring the Docker stack up (idempotent; blocks until backend+frontend are healthy)
# -----------------------------------------------------------------------------
if docker info >/dev/null 2>&1; then
    phase "docker stack up" bash scripts/start.sh || OVERALL_FAILED=1
else
    phase_skip "docker stack up" "docker not running — preflight's server-backed suites will fail"
fi

# -----------------------------------------------------------------------------
# 3. Everything preflight.sh --full covers (format, lint, backend, frontend,
#    Bruno API tests, Playwright E2E, Docker build/smoke — see preflight.sh --list)
# -----------------------------------------------------------------------------
phase "preflight --full" bash scripts/preflight.sh --full || OVERALL_FAILED=1

# -----------------------------------------------------------------------------
# 4. markdown-lint — ratchet vs origin/main (same policy as the Spotless gate,
#    issue #705): only files this branch actually touches are linted, so the
#    thousands of pre-existing violations across docs/ are never in scope.
# -----------------------------------------------------------------------------
git fetch origin main --quiet 2>/dev/null
mapfile -t CHANGED_MD < <(git diff --name-only origin/main...HEAD -- '*.md' 2>/dev/null | while read -r f; do [ -f "$f" ] && echo "$f"; done)

if [ ! -x frontend/node_modules/.bin/markdownlint-cli2 ]; then
    phase_skip "markdown-lint" "frontend/node_modules not installed — run 'cd frontend && npm ci'"
elif [ "${#CHANGED_MD[@]}" -eq 0 ]; then
    phase_skip "markdown-lint" "no *.md files changed vs origin/main"
else
    # --no-globs: without it, the config's "globs" property (needed so a bare
    # `markdownlint-cli2` run still has a sane default) overrides these explicit
    # ratcheted paths and lints the whole repo instead.
    phase "markdown-lint" frontend/node_modules/.bin/markdownlint-cli2 --no-globs "${CHANGED_MD[@]}" || OVERALL_FAILED=1
fi

# -----------------------------------------------------------------------------
# 5. Best-effort dashboard artifacts (never fail the pipeline over a report)
# -----------------------------------------------------------------------------
if [ -d frontend/node_modules ]; then
    (cd frontend && ./node_modules/.bin/eslint src -f html -o "$REPORT_DIR/eslint-report.html") >/dev/null 2>&1 || true
fi
mvn checkstyle:checkstyle -pl backend-api --no-transfer-progress -q >/dev/null 2>&1 || true

# -----------------------------------------------------------------------------
# Dashboard
# -----------------------------------------------------------------------------
PIPELINE_END="$(date +%s)"
TOTAL_DURATION=$((PIPELINE_END - PIPELINE_START))
[ "$OVERALL_FAILED" -eq 0 ] && OVERALL_LABEL="PASSED" OVERALL_COLOR="#1a7f37" || OVERALL_LABEL="FAILED" OVERALL_COLOR="#cf222e"

DASHBOARD="$REPORT_DIR/index.html"
{
    echo "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\">"
    echo "<title>run_pipeline.sh — $TIMESTAMP</title>"
    echo "<style>body{font-family:-apple-system,sans-serif;max-width:900px;margin:2rem auto;padding:0 1rem}"
    echo "h1{color:$OVERALL_COLOR} table{width:100%;border-collapse:collapse} td,th{padding:8px;border-bottom:1px solid #ddd;text-align:left}"
    echo ".PASS{color:#1a7f37;font-weight:600} .FAIL{color:#cf222e;font-weight:600} .SKIP{color:#9a6700;font-weight:600}"
    echo "a{color:#0969da}</style></head><body>"
    echo "<h1>run_pipeline.sh — $OVERALL_LABEL</h1>"
    echo "<p>Run: $TIMESTAMP &middot; Duration: ${TOTAL_DURATION}s &middot; Branch: $(git rev-parse --abbrev-ref HEAD)</p>"
    echo "<table><tr><th>Gate</th><th>Status</th><th>Duration</th><th>Log</th></tr>"
    for i in "${!PHASE_NAMES[@]}"; do
        name="${PHASE_NAMES[$i]}"; status="${PHASE_STATUSES[$i]}"; dur="${PHASE_DURATIONS[$i]}"; logf="${PHASE_LOGS[$i]}"
        link="—"
        [ -n "$logf" ] && link="<a href=\"file://$logf\">log</a>"
        echo "<tr><td>$name</td><td class=\"$status\">$status</td><td>${dur}s</td><td>$link</td></tr>"
    done
    echo "</table><h2>Artifacts</h2><ul>"
    [ -f "$REPO_ROOT/backend-api/target/site/jacoco/index.html" ] && \
        echo "<li><a href=\"file://$REPO_ROOT/backend-api/target/site/jacoco/index.html\">Backend coverage (JaCoCo)</a></li>"
    [ -f "$REPO_ROOT/backend-api/target/checkstyle-result.html" ] && \
        echo "<li><a href=\"file://$REPO_ROOT/backend-api/target/checkstyle-result.html\">Backend style (Checkstyle)</a></li>"
    [ -f "$REPO_ROOT/backend-api/target/spotbugsXml.xml" ] && \
        echo "<li><a href=\"file://$REPO_ROOT/backend-api/target/spotbugsXml.xml\">Backend bug patterns (SpotBugs XML)</a></li>"
    [ -f "$REPORT_DIR/eslint-report.html" ] && \
        echo "<li><a href=\"file://$REPORT_DIR/eslint-report.html\">Frontend lint (ESLint)</a></li>"
    [ -f "$REPO_ROOT/frontend/playwright-report/index.html" ] && \
        echo "<li><a href=\"file://$REPO_ROOT/frontend/playwright-report/index.html\">E2E tests (Playwright)</a></li>"
    echo "</ul><p><a href=\"file://$SUMMARY_LOG\">Full pipeline log</a></p>"
    echo "</body></html>"
} > "$DASHBOARD"

printf '\n%s================ PIPELINE SUMMARY ================%s\n' "$BLUE" "$NC"
printf 'dashboard: %s\n' "$DASHBOARD"
printf 'log:       %s\n' "$SUMMARY_LOG"
if [ "$OVERALL_FAILED" -eq 0 ]; then
    printf '%sPIPELINE PASSED%s (%ds) — ready for a PR.\n' "$GREEN" "$NC" "$TOTAL_DURATION"
    exit 0
else
    printf '%sPIPELINE FAILED%s (%ds) — do not open a PR until this is green.\n' "$RED" "$NC" "$TOTAL_DURATION"
    exit 1
fi
