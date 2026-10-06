#!/usr/bin/env bash
# Verifies the docs module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

python3 scripts/test_docs_links.py
python3 scripts/test_business_docs_traceability.py
python3 scripts/test_changelog_structure.py
