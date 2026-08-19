#!/usr/bin/env bash
# ============================================================
# generate-github-page-metrics.sh
#
# Generates github-page/public/metrics.json from repo/test data.
# Shared by ci.yml's deploy-pages job and deploy-github-page.yml so the two
# workflows can't drift out of sync (issue #758) -- previously each had its
# own copy of this logic, including the same bugs in both places.
#
# Reads, if present in the current working directory:
#   coverage-snapshot/coverage-snapshot.json   -- backend JaCoCo numbers
#   frontend-coverage-dl/coverage/coverage-summary.json -- frontend Vitest numbers
#
# When either is missing (a generation hiccup that run), falls back to the
# corresponding field from the currently-LIVE deployed metrics.json instead
# of a fabricated default -- never silently deploy a misleading "0%".
#
# Requires: git, jq, curl (all present on ubuntu-latest runners).
# Optional env: GH_REPO (defaults to matiaspakua/notaire), LIVE_METRICS_URL.
# ============================================================
set -euo pipefail

REPO="${GH_REPO:-matiaspakua/notaire}"
LIVE_METRICS_URL="${LIVE_METRICS_URL:-https://matiaspakua.github.io/notaire/metrics.json}"
OUTPUT_FILE="github-page/public/metrics.json"

COMMIT_COUNT=$(git rev-list --count HEAD)
PR_COUNT=$(gh api "search/issues?q=repo:${REPO}+type:pr&per_page=1" --jq '.total_count' 2>/dev/null || echo "0")
MD_COUNT=$(find . -name "*.md" -not -path "*/node_modules/*" -not -path "*/.git/*" | wc -l | tr -d ' ')
BACKEND_TEST_COUNT=$(find backend-api/src/test -name "*Test.java" 2>/dev/null | wc -l | tr -d ' ')
FRONTEND_TEST_COUNT=$(find frontend/tests -name "*.spec.*" 2>/dev/null | wc -l | tr -d ' ')
TEST_COUNT=$(( BACKEND_TEST_COUNT + FRONTEND_TEST_COUNT ))
API_TEST_COUNT=$(find backend-api/api-test -name "*.yml" -not -name "folder.yml" 2>/dev/null | wc -l | tr -d ' ')
ADR_COUNT=$(find docs -name "ADR-*.md" 2>/dev/null | wc -l | tr -d ' ')
UC_COUNT=$(find docs/100-business/102-use-cases -type f -name "CU*.md" 2>/dev/null | wc -l | tr -d ' ')
RF_FILE="docs/100-business/101-requirements/requerimientos.csv"
RF_COUNT=$(tail -n +2 "$RF_FILE" 2>/dev/null | wc -l | tr -d ' ')
DC1=$(yq '.services | length' docker-compose.yml 2>/dev/null || echo "0")
DC2=$(yq '.services | length' infra/docker-compose.yml 2>/dev/null || echo "0")
DOCKER_COUNT=$(( DC1 + DC2 ))
GRAFANA_COUNT=$(find infra/grafana/provisioning/dashboards -name "*.json" 2>/dev/null | wc -l | tr -d ' ')
CONTRIBUTORS=$(git log --format="%aE" | sort -u | wc -l | tr -d ' ')
# tail (not `| head -1` on a --reverse log) so the producer is never cut off
# early -- head closing the pipe on a large reversed log can SIGPIPE git under
# `set -o pipefail`.
FIRST_COMMIT=$(git log --format="%aI" | tail -1)

# Last known-good deployed metrics -- the fallback source of truth when this
# run can't produce fresh numbers for a given field. Never fatal if this is
# unreachable (e.g. first-ever deploy): falls through to the literal defaults
# passed to live_field below.
LIVE_METRICS=$(curl -sf "$LIVE_METRICS_URL" 2>/dev/null || echo '{}')
LIVE_COVERAGE=$(echo "$LIVE_METRICS" | jq -c '.coverage // {}' 2>/dev/null || echo '{}')

live_field() {
  # $1 = field name, $2 = default if not present live
  echo "$LIVE_COVERAGE" | jq -r --arg k "$1" --arg d "$2" '.[$k] // $d' 2>/dev/null || echo "$2"
}

BACKEND_SNAPSHOT="coverage-snapshot/coverage-snapshot.json"
if [ -f "$BACKEND_SNAPSHOT" ]; then
  BACKEND_INSTRUCTION=$(jq -r '.backendInstruction' "$BACKEND_SNAPSHOT")
  BACKEND_BRANCH=$(jq -r '.backendBranch' "$BACKEND_SNAPSHOT")
  COVERAGE_UPDATED=$(jq -r '.lastUpdated' "$BACKEND_SNAPSHOT")
else
  echo "::warning::backend coverage-snapshot.json missing this run -- reusing last deployed values instead of fabricating zeros"
  BACKEND_INSTRUCTION=$(live_field backendInstruction 0)
  BACKEND_BRANCH=$(live_field backendBranch 0)
  COVERAGE_UPDATED=$(live_field lastUpdated "unknown")
fi

FRONTEND_SUMMARY="frontend-coverage-dl/coverage/coverage-summary.json"
if [ -f "$FRONTEND_SUMMARY" ]; then
  FRONTEND_COMPONENT=$(jq -r '.total.statements.pct | floor' "$FRONTEND_SUMMARY")
else
  echo "::warning::frontend coverage-summary.json missing this run -- reusing last deployed value instead of a hardcoded constant"
  FRONTEND_COMPONENT=$(live_field frontendComponent 0)
fi

# Not recomputed fresh this pass (tracked as a scoped follow-up per issue
# #758) -- carry the last deployed value forward rather than a hardcoded
# constant, so at minimum these never regress to a stale/fake default.
E2E_TESTS=$(live_field e2eTests 0)
API_ENDPOINTS=$(live_field apiEndpoints 0)

COVERAGE_JSON=$(jq -n \
  --argjson backendInstruction "$BACKEND_INSTRUCTION" \
  --argjson backendBranch "$BACKEND_BRANCH" \
  --argjson frontendComponent "$FRONTEND_COMPONENT" \
  --argjson e2eTests "$E2E_TESTS" \
  --argjson apiEndpoints "$API_ENDPOINTS" \
  --arg lastUpdated "$COVERAGE_UPDATED" \
  '{backendInstruction:$backendInstruction, backendBranch:$backendBranch, frontendComponent:$frontendComponent, e2eTests:$e2eTests, apiEndpoints:$apiEndpoints, lastUpdated:$lastUpdated}')

mkdir -p github-page/public
GENERATED_AT=$(date -u +%Y-%m-%dT%H:%M:%SZ)
{
  printf '%s\n' '{'
  printf '  "generatedAt": "%s",\n' "${GENERATED_AT}"
  printf '  "commits": %s,\n' "${COMMIT_COUNT}"
  printf '  "pullRequests": %s,\n' "${PR_COUNT}"
  printf '  "markdownFiles": %s,\n' "${MD_COUNT}"
  printf '  "testClasses": %s,\n' "${TEST_COUNT}"
  printf '  "backendTestClasses": %s,\n' "${BACKEND_TEST_COUNT}"
  printf '  "frontendTestClasses": %s,\n' "${FRONTEND_TEST_COUNT}"
  printf '  "apiTestFiles": %s,\n' "${API_TEST_COUNT}"
  printf '  "adrCount": %s,\n' "${ADR_COUNT}"
  printf '  "useCases": %s,\n' "${UC_COUNT}"
  printf '  "functionalRequirements": %s,\n' "${RF_COUNT}"
  printf '  "dockerServices": %s,\n' "${DOCKER_COUNT}"
  printf '  "grafanaDashboards": %s,\n' "${GRAFANA_COUNT}"
  printf '  "aiTools": 4,\n'
  printf '  "contributors": %s,\n' "${CONTRIBUTORS}"
  printf '  "firstCommitDate": "%s",\n' "${FIRST_COMMIT}"
  printf '  "coverage": %s\n' "${COVERAGE_JSON}"
  printf '%s\n' '}'
} > "$OUTPUT_FILE"

echo "=== metrics.json ==="
cat "$OUTPUT_FILE"
