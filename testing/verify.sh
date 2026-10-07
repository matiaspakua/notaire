#!/usr/bin/env bash
# Verifies the testing module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

(cd testing/e2e && ./node_modules/.bin/tsc --noEmit && ./node_modules/.bin/eslint . --max-warnings=0)
python3 testing/tests/test_testing_standalone.py
python3 testing/tests/test_e2e_reliability.py
python3 testing/tests/test_generate_e2e_coverage_report.py
