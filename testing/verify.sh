#!/usr/bin/env bash
# Verifies the testing module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

(cd testing/e2e && ./node_modules/.bin/tsc --noEmit && ./node_modules/.bin/eslint . --max-warnings=0)
python3 scripts/test_testing_standalone.py
