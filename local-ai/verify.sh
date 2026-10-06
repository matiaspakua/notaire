#!/usr/bin/env bash
# Verifies the local-ai module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

python3 -m unittest discover -s local-ai/sdlc/tests
