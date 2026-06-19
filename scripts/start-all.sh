#!/bin/bash

# Notaire — Start the FULL system: application + infrastructure.
#
# 1. Application stack  (PostgreSQL, Backend, Frontend, pgAdmin)  -> scripts/start.sh
# 2. Infrastructure     (Prometheus, Grafana, Loki, Promtail,
#                        postgres-exporter, Homer, SonarQube)     -> infra/scripts/start-infra.sh
#
# All credentials come from the single root .env file (git-ignored).
# Any arguments are forwarded to scripts/start.sh (e.g. --skip-build).

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

bash "$SCRIPT_DIR/start.sh" "$@"
bash "$SCRIPT_DIR/../infra/scripts/start-infra.sh"
