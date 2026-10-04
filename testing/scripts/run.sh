#!/usr/bin/env bash
#
# run.sh — single entry point for the Notaire QA suites.
#
#   bash testing/scripts/run.sh --list
#   bash testing/scripts/run.sh integration   # cURL suite + stack smoke against a RUNNING stack
#   bash testing/scripts/run.sh database      # empty PostgreSQL -> Flyway -> SQL checks (Docker only)
#   bash testing/scripts/run.sh e2e [args]    # Playwright UI suite against a RUNNING stack (extra args go to playwright)
#
# Variables (see testing/.env.example): BASE_URL, E2E_BASE_URL, MIGRATIONS_DIR, POSTGRES_EXPORTER_*.
# Env file lookup: $TESTING_ENV_FILE, then testing/.env.

set -euo pipefail

TESTING_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SUITES=(integration database e2e)

usage() {
    echo "Usage: bash testing/scripts/run.sh <suite>|--list"
    echo "Suites: ${SUITES[*]}"
}

load_env() {
    local file="${TESTING_ENV_FILE:-$TESTING_DIR/.env}"
    if [ -f "$file" ]; then
        set -a
        # shellcheck disable=SC1090
        . "$file"
        set +a
    fi
}

run_integration() {
    local base_url="${BASE_URL:-http://localhost:8080}"
    if ! curl -sf "$base_url/actuator/health" > /dev/null 2>&1 \
        && ! curl -sf "$base_url/swagger-ui.html" > /dev/null 2>&1; then
        echo "The API is not reachable at $base_url. Start the application first, or set BASE_URL." >&2
        exit 1
    fi
    export BASE_URL="$base_url"
    (cd "$TESTING_DIR/integration/http" && bash test-all-endpoints-v2.sh)
    bash "$TESTING_DIR/integration/e2e-login-and-stack.sh"
}

run_database() {
    bash "$TESTING_DIR/database/run.sh"
}

run_e2e() {
    local ui_url="${E2E_BASE_URL:-http://localhost:3000}"
    if ! curl -sf -o /dev/null "$ui_url" > /dev/null 2>&1; then
        echo "The UI is not reachable at $ui_url. Start the application first, or set E2E_BASE_URL." >&2
        exit 1
    fi
    if [ ! -d "$TESTING_DIR/e2e/node_modules" ]; then
        (cd "$TESTING_DIR/e2e" && npm ci)
    fi
    (cd "$TESTING_DIR/e2e" && BASE_URL="$ui_url" npx playwright test "$@")
}

case "${1:-}" in
    --list)
        printf '%s\n' "${SUITES[@]}"
        ;;
    integration)
        load_env
        run_integration
        ;;
    database)
        load_env
        run_database
        ;;
    e2e)
        load_env
        shift
        run_e2e "$@"
        ;;
    -h|--help|"")
        usage
        [ -n "${1:-}" ] || exit 2
        ;;
    *)
        echo "Unknown suite: $1" >&2
        usage >&2
        exit 2
        ;;
esac
