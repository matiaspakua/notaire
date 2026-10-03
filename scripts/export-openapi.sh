#!/usr/bin/env bash
#
# export-openapi.sh — regenerate the committed OpenAPI artifact (#1067 / CU76).
#
# Usage:
#   bash scripts/export-openapi.sh           # prefer live API, else Maven/H2
#   bash scripts/export-openapi.sh --maven   # force Maven H2 export (CI)
#   bash scripts/export-openapi.sh --live    # require http://localhost:8080
#
# Output (stable path): backend-api/openapi/openapi.yaml
#
# Developers: after changing REST controllers/DTOs/annotations, regenerate and
# commit the artifact in the same PR. The OpenAPI Contract workflow fails if
# the file is stale or introduces breaking changes vs main.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

OUT="backend-api/openapi/openapi.yaml"
BASE_URL="${OPENAPI_BASE_URL:-http://localhost:8080}"
MODE="auto"

for arg in "$@"; do
  case "$arg" in
    --maven) MODE="maven" ;;
    --live)  MODE="live" ;;
    -h|--help)
      sed -n '3,16p' "$0"
      exit 0
      ;;
    *)
      echo "unknown option: $arg" >&2
      exit 2
      ;;
  esac
done

mkdir -p "$(dirname "$OUT")"

export_live() {
  echo "Exporting OpenAPI from ${BASE_URL}/v3/api-docs.yaml ..."
  curl -fsS "${BASE_URL}/v3/api-docs.yaml" -o "$OUT"
}

export_maven() {
  echo "Exporting OpenAPI via springdoc (Maven test-h2 / OpenApiSpecExportIT) ..."
  ABS_OUT="$(cd "$(dirname "$OUT")" && pwd)/$(basename "$OUT")"
  mvn -pl backend-api -am test \
    -Dtest=OpenApiSpecExportIT \
    -Dsurefire.failIfNoSpecifiedTests=false \
    -DfailIfNoTests=false \
    -Dopenapi.export.path="$ABS_OUT" \
    -q
}

case "$MODE" in
  live)
    export_live
    ;;
  maven)
    export_maven
    ;;
  auto)
    if curl -fsS -o /dev/null "${BASE_URL}/actuator/health" 2>/dev/null \
      || curl -fsS -o /dev/null "${BASE_URL}/v3/api-docs.yaml" 2>/dev/null; then
      export_live
    else
      echo "Live API not reachable at ${BASE_URL}; falling back to Maven/H2 export."
      export_maven
    fi
    ;;
esac

if [ ! -s "$OUT" ]; then
  echo "ERROR: export produced empty or missing $OUT" >&2
  exit 1
fi

# Canonicalize YAML: springdoc property maps use unordered HashMaps, so key
# order can differ across JVMs/runs and trip the CI freshness git-diff gate.
# Sort every mapping by key; preserve sequence order; stable dump + trailing NL.
if ! python3 -c 'import yaml' >/dev/null 2>&1; then
  python3 -m pip install --user -q 'PyYAML>=6'
fi
python3 - <<'PY' "$OUT"
import pathlib
import sys

import yaml

path = pathlib.Path(sys.argv[1])


class SortedDumper(yaml.SafeDumper):
    """Emit mappings with sorted keys for deterministic OpenAPI artifacts."""

    def ignore_aliases(self, data):
        return True


def _represent_dict(dumper, data):
    return dumper.represent_mapping(
        yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG,
        sorted(data.items(), key=lambda item: str(item[0])),
    )


SortedDumper.add_representer(dict, _represent_dict)

raw = path.read_text(encoding="utf-8")
doc = yaml.safe_load(raw)
if doc is None:
    raise SystemExit(f"ERROR: empty OpenAPI document at {path}")


def sort_maps(node):
    if isinstance(node, dict):
        return {k: sort_maps(node[k]) for k in sorted(node, key=str)}
    if isinstance(node, list):
        return [sort_maps(item) for item in node]
    return node


canonical = sort_maps(doc)
text = yaml.dump(
    canonical,
    Dumper=SortedDumper,
    default_flow_style=False,
    allow_unicode=True,
    sort_keys=False,  # SortedDumper already sorts
    width=120,
)
path.write_text(text.rstrip() + "\n", encoding="utf-8")
PY

echo "Wrote $OUT ($(wc -c < "$OUT") bytes)"
echo "Commit this file when the API contract intentionally changes."
