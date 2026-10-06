#!/usr/bin/env bash
# Verifies the docs module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

python3 docs/tests/test_docs_links.py
python3 docs/tests/test_business_docs_traceability.py
python3 docs/tests/test_changelog_structure.py
python3 docs/tests/test_erd_current_schema.py
python3 docs/tests/test_data_dictionary_sync.py
python3 docs/tests/test_generate_data_dictionary.py
