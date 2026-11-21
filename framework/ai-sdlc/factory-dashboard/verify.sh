#!/usr/bin/env bash
# Factory dashboard gate: install from the lockfile, typecheck, server tests, UI build.
# --e2e also runs Playwright + axe against a seeded `aisdlc serve` (needs python3 and Playwright's Chromium).
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"
pnpm install --frozen-lockfile --silent
pnpm typecheck
pnpm test
pnpm build
if [[ "${1:-}" == "--e2e" ]]; then
  pnpm e2e
fi
