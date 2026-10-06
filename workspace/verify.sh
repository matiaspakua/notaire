#!/usr/bin/env bash
# Verifies the workspace module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

python3 workspace/tests/test_modules_manifest.py
bash scripts/check-agent-rules.sh
bash scripts/validate-sdlc-plan.sh
