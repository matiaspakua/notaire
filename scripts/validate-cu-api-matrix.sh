#!/usr/bin/env bash
# Thin wrapper so callers can use validate-cu-api-matrix.sh or .py (#1064).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
exec python3 "$ROOT/scripts/validate-cu-api-matrix.py" "$@"
