#!/usr/bin/env bash
# Verifies the workspace module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

python3 workspace/tests/test_modules_manifest.py
bash workspace/sdlc/check-agent-rules.sh
bash workspace/sdlc/validate-sdlc-plan.sh
python3 workspace/tests/test_modules_cli.py
python3 workspace/tests/test_openspec_location.py
