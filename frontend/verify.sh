#!/usr/bin/env bash
# Verifies the frontend module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

cd frontend
./node_modules/.bin/tsc --noEmit
./node_modules/.bin/eslint src --max-warnings=0
./node_modules/.bin/vitest run
./node_modules/.bin/next build
