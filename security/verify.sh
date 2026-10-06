#!/usr/bin/env bash
# Verifies the security module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

bash -n security/apply-protect-main-ruleset.sh
bash -n security/assert-protect-main-ruleset.sh
python3 security/tests/test_protect_main_ruleset.py
