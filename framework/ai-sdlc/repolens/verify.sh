#!/usr/bin/env bash
# repolens module gate: lint, tests with a coverage floor, wheel build and a CLI smoke run. Any directory.
# PYTHON overrides the interpreter (e.g. PYTHON=.venv/bin/python bash verify.sh).
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$HERE"
PY="${PYTHON:-python3}"
COVERAGE_FLOOR="${COVERAGE_FLOOR:-88}"

echo "== lint"
"$PY" -m ruff check repolens tests bench

echo "== tests (coverage floor ${COVERAGE_FLOOR}%)"
"$PY" -m pytest -q -p no:cacheprovider --cov=repolens --cov-report=term --cov-fail-under="$COVERAGE_FLOOR"

echo "== package"
rm -rf build dist ./*.egg-info
"$PY" -m pip wheel --no-deps --no-build-isolation -q -w dist . >/dev/null
ls dist/repolens-*.whl
rm -rf build ./*.egg-info

echo "== smoke"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
# A private home (registry, vector cache) and no resident service: the smoke run leaves nothing behind.
export REPOLENS_HOME="$TMP/home" REPOLENS_VECTOR_CACHE="$TMP/home/vectors" REPOLENS_DAEMON=0
mkdir -p "$TMP/src"
printf 'def greet(name):\n    return f"hi {name}"\n' > "$TMP/src/app.py"
"$PY" -m repolens --db "$TMP/g.db" index "$TMP/src" --name smoke --embedder lsa >/dev/null
OUT="$("$PY" -m repolens --db "$TMP/g.db" search "greet" -k 1)"
[[ "$OUT" == *greet* ]] || { echo "smoke search did not find greet: $OUT" >&2; exit 1; }
echo "verify: OK"
