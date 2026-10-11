#!/bin/bash
# Notaire — verify that metrics, logs, traces, alerts and Sonar hold real data (issue #1469).
#
#   bash infra/scripts/verify-signals.sh              # check the signals produced so far
#   bash infra/scripts/verify-signals.sh --with-e2e   # run the Playwright suite first to generate traffic
#
# Credentials come from the infra env file (see common.sh); the checks use GRAFANA_ADMIN_* and SONAR_ADMIN_*.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
. "$SCRIPT_DIR/common.sh"
ENV_FILE="$(resolve_env_file)"
# shellcheck disable=SC1090
set -a; [ -f "$ENV_FILE" ] && . "$ENV_FILE"; set +a

if [ "${1:-}" = "--with-e2e" ]; then
    echo "=== Playwright E2E (generates traffic) ==="
    bash "$APP_DIR/testing/scripts/run.sh" e2e
    echo "=== Letting Promtail and Prometheus catch up ==="
    sleep 30
fi

echo "=== Observability signals ==="
python3 "$SCRIPT_DIR/verify_signals.py"
