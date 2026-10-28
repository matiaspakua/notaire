#!/usr/bin/env bash
# Verifies the ai-sdlc-framework module only: its self-tests and the adopter profile. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

python3 -m unittest discover -s framework/ai-sdlc/tests
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml doctor
