#!/usr/bin/env bash
# Verifies the infra module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

python3 scripts/test_infra_standalone.py
python3 scripts/test_infra_prometheus_hardening.py
python3 scripts/test_staging_kustomize.py
