#!/bin/bash
# Notaire — verify that metrics, logs, traces, alerts and Sonar hold real data (issue #1469).
#
#   bash infra/scripts/verify-signals.sh              # check the signals produced so far
#   bash infra/scripts/verify-signals.sh --with-e2e   # run the Playwright suite first to generate traffic
#   bash infra/scripts/verify-signals.sh --with-grafana-panels
#       # also assert key Grafana dashboard UIDs + panel PromQL series (#1735)
#   Flags may be combined: --with-e2e --with-grafana-panels
#
# Credentials come from the infra env file (see common.sh); the checks use GRAFANA_ADMIN_* and SONAR_ADMIN_*.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
. "$SCRIPT_DIR/common.sh"
ENV_FILE="$(resolve_env_file)"
# shellcheck disable=SC1090
set -a; [ -f "$ENV_FILE" ] && . "$ENV_FILE"; set +a

WITH_E2E=0
WITH_GRAFANA_PANELS=0
for arg in "$@"; do
    case "$arg" in
        --with-e2e) WITH_E2E=1 ;;
        --with-grafana-panels) WITH_GRAFANA_PANELS=1 ;;
        *)
            echo "Unknown argument: $arg" >&2
            echo "Usage: bash infra/scripts/verify-signals.sh [--with-e2e] [--with-grafana-panels]" >&2
            exit 2
            ;;
    esac
done

if [ "$WITH_E2E" = 1 ]; then
    echo "=== Playwright E2E (generates traffic) ==="
    bash "$APP_DIR/testing/scripts/run.sh" e2e
    echo "=== Letting Promtail and Prometheus catch up ==="
    sleep 30
fi

echo "=== Observability signals ==="
python3 "$SCRIPT_DIR/verify_signals.py"

if [ "$WITH_GRAFANA_PANELS" = 1 ]; then
    echo "=== Grafana panel series (#1735) ==="
    python3 "$SCRIPT_DIR/verify_grafana_panels.py"
fi
