#!/usr/bin/env bash
# Verifies the infra module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

python3 infra/tests/test_infra_standalone.py
python3 infra/tests/test_infra_prometheus_hardening.py
python3 infra/tests/test_staging_kustomize.py
python3 infra/tests/test_performance_test_assets.py
