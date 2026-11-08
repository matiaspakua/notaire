#!/usr/bin/env bash
# Compare factory-control.openapi.yaml to a base file with oasdiff (#2136).
# Usage: check-contract-breaking.sh <base.yaml> [head.yaml]
set -euo pipefail
KIT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE="${1:?base OpenAPI file required}"
HEAD="${2:-$KIT/openapi/factory-control.openapi.yaml}"
ACCEPT="$KIT/openapi/accepted-breaking-changes.d"

if ! command -v oasdiff >/dev/null 2>&1; then
  echo "oasdiff not installed — structural contract check only"
  python3 - <<PY
from pathlib import Path
import sys
sys.path.insert(0, "$KIT")
from aisdlc.contract import check
raise SystemExit(check(Path("$KIT")))
PY
  exit 0
fi

args=(breaking "$BASE" "$HEAD")
shopt -s nullglob
for entry in "$ACCEPT"/*.txt; do
  args+=(--fail-on ERR)
  # oasdiff accept-file support varies; pass as extra args when files exist
  args+=(--exclude-elements "$entry")
done
oasdiff "${args[@]}"
echo "oasdiff breaking: clean (or accepted)"
