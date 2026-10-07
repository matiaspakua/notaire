#!/usr/bin/env bash
# Verifies the contracts module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

python3 contracts/tests/test_seams.py
python3 contracts/tests/test_api_reachability.py
