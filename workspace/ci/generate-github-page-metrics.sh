#!/usr/bin/env bash
# ============================================================
# generate-github-page-metrics.sh
#
# Generates github-page/public/metrics.json from repo/test data.
# Shared by ci.yml's deploy-pages job and deploy-github-page.yml so the two
# workflows can't drift out of sync (issue #758) -- previously each had its
# own copy of this logic, including the same bugs in both places.
#
# Reads, if present:
#   coverage-snapshot/coverage-snapshot.json   -- backend JaCoCo numbers
#   frontend-coverage-dl/coverage/coverage-summary.json -- frontend Vitest numbers
# Paths may be overridden with BACKEND_SNAPSHOT / FRONTEND_SUMMARY (tests).
#
# When either is missing (a generation hiccup that run), falls back to the
# corresponding field from the currently-LIVE deployed metrics.json only when
# that live coverage was itself measured (lastUpdated != "unknown"). Otherwise
# writes JSON null -- never silently deploy a misleading "0%" (#2203).
#
# Requires: git, jq, curl (all present on ubuntu-latest runners).
# Optional env: GH_REPO, LIVE_METRICS_URL, OUTPUT_FILE, BACKEND_SNAPSHOT,
#               FRONTEND_SUMMARY.
# ============================================================
set -euo pipefail

REPO="${GH_REPO:-matiaspakua/notaire}"
LIVE_METRICS_URL="${LIVE_METRICS_URL:-https://matiaspakua.github.io/notaire/metrics.json}"
OUTPUT_FILE="${OUTPUT_FILE:-github-page/public/metrics.json}"

COMMIT_COUNT=$(git rev-list --count HEAD)
PR_COUNT=$(gh api "search/issues?q=repo:${REPO}+type:pr&per_page=1" --jq '.total_count' 2>/dev/null || echo "0")
MD_COUNT=$(find . -name "*.md" -not -path "*/node_modules/*" -not -path "*/.git/*" | wc -l | tr -d ' ')
BACKEND_TEST_COUNT=$(find backend-api/src/test -name "*Test.java" 2>/dev/null | wc -l | tr -d ' ')
# Vitest unit/component tests (issue #1982) — not Playwright.
FRONTEND_TEST_COUNT=$(find frontend/src \( -name "*.test.ts" -o -name "*.test.tsx" \) 2>/dev/null | wc -l | tr -d ' ')
E2E_SPEC_COUNT=$(find testing/e2e/tests -name "*.spec.*" 2>/dev/null | wc -l | tr -d ' ')
TEST_COUNT=$(( BACKEND_TEST_COUNT + FRONTEND_TEST_COUNT + E2E_SPEC_COUNT ))
API_TEST_COUNT=$(find backend-api/api-test -name "*.yml" -not -name "folder.yml" 2>/dev/null | wc -l | tr -d ' ')
ADR_COUNT=$(find docs -name "ADR-*.md" 2>/dev/null | wc -l | tr -d ' ')
UC_COUNT=$(find docs/100-business/102-use-cases -type f -name "CU*.md" 2>/dev/null | wc -l | tr -d ' ')
RF_FILE="docs/100-business/101-requirements/requerimientos.csv"
RF_COUNT=$(tail -n +2 "$RF_FILE" 2>/dev/null | wc -l | tr -d ' ')
DC1=$(yq '.services | length' docker-compose.yml 2>/dev/null || echo "0")
DC2=$(yq '.services | length' infra/observability/docker-compose.yml 2>/dev/null || echo "0")
DOCKER_COUNT=$(( DC1 + DC2 ))
GRAFANA_COUNT=$(find infra/observability/grafana/provisioning/dashboards -name "*.json" 2>/dev/null | wc -l | tr -d ' ')
CONTRIBUTORS=$(git log --format="%aE" | sort -u | wc -l | tr -d ' ')
# tail (not `| head -1` on a --reverse log) so the producer is never cut off
# early -- head closing the pipe on a large reversed log can SIGPIPE git under
# `set -o pipefail`.
FIRST_COMMIT=$(git log --format="%aI" | tail -1)

# Last known-good deployed metrics -- the fallback source of truth when this
# run can't produce fresh numbers for a given field. Never fatal if this is
# unreachable (e.g. first-ever deploy).
LIVE_METRICS=$(curl -sf "$LIVE_METRICS_URL" 2>/dev/null || echo '{}')
LIVE_COVERAGE=$(echo "$LIVE_METRICS" | jq -c '.coverage // {}' 2>/dev/null || echo '{}')
LIVE_UPDATED=$(echo "$LIVE_COVERAGE" | jq -r '.lastUpdated // "unknown"' 2>/dev/null || echo "unknown")
LIVE_IS_MEASURED=0
if [ -n "$LIVE_UPDATED" ] && [ "$LIVE_UPDATED" != "unknown" ] && [ "$LIVE_UPDATED" != "null" ]; then
  LIVE_IS_MEASURED=1
fi

# Emit a jq-ready JSON token for a coverage field. Prefer a live numeric value
# only when the live stamp is measured; otherwise null (never a fabricated 0).
coverage_fallback() {
  local key="$1"
  if [ "$LIVE_IS_MEASURED" -eq 1 ]; then
    local value
    value=$(echo "$LIVE_COVERAGE" | jq -c --arg k "$key" '.[$k] // null' 2>/dev/null || echo "null")
    if [ -z "$value" ] || [ "$value" = "" ]; then
      echo "null"
    else
      echo "$value"
    fi
  else
    echo "null"
  fi
}

BACKEND_SNAPSHOT="${BACKEND_SNAPSHOT:-coverage-snapshot/coverage-snapshot.json}"
if [ -f "$BACKEND_SNAPSHOT" ]; then
  BACKEND_INSTRUCTION=$(jq -c '.backendInstruction' "$BACKEND_SNAPSHOT")
  BACKEND_BRANCH=$(jq -c '.backendBranch' "$BACKEND_SNAPSHOT")
  COVERAGE_UPDATED=$(jq -r '.lastUpdated' "$BACKEND_SNAPSHOT")
else
  echo "::warning::backend coverage-snapshot.json missing this run -- reusing measured live values only (never fabricating zeros)"
  BACKEND_INSTRUCTION=$(coverage_fallback backendInstruction)
  BACKEND_BRANCH=$(coverage_fallback backendBranch)
  if [ "$LIVE_IS_MEASURED" -eq 1 ]; then
    COVERAGE_UPDATED="$LIVE_UPDATED"
  else
    COVERAGE_UPDATED="unknown"
  fi
fi

FRONTEND_SUMMARY="${FRONTEND_SUMMARY:-frontend-coverage-dl/coverage/coverage-summary.json}"
if [ -f "$FRONTEND_SUMMARY" ]; then
  FRONTEND_COMPONENT=$(jq -c '.total.statements.pct | floor' "$FRONTEND_SUMMARY")
else
  echo "::warning::frontend coverage-summary.json missing this run -- reusing measured live value only (never a hardcoded zero)"
  FRONTEND_COMPONENT=$(coverage_fallback frontendComponent)
fi

# Not recomputed fresh this pass (tracked as a scoped follow-up per issue
# #758 / #760) -- carry the last *measured* deployed value forward rather than
# a hardcoded zero.
E2E_TESTS=$(coverage_fallback e2eTests)
API_ENDPOINTS=$(coverage_fallback apiEndpoints)

COVERAGE_JSON=$(jq -n \
  --argjson backendInstruction "$BACKEND_INSTRUCTION" \
  --argjson backendBranch "$BACKEND_BRANCH" \
  --argjson frontendComponent "$FRONTEND_COMPONENT" \
  --argjson e2eTests "$E2E_TESTS" \
  --argjson apiEndpoints "$API_ENDPOINTS" \
  --arg lastUpdated "$COVERAGE_UPDATED" \
  '{backendInstruction:$backendInstruction, backendBranch:$backendBranch, frontendComponent:$frontendComponent, e2eTests:$e2eTests, apiEndpoints:$apiEndpoints, lastUpdated:$lastUpdated}')

mkdir -p "$(dirname "$OUTPUT_FILE")"
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
  printf '  "e2eSpecFiles": %s,\n' "${E2E_SPEC_COUNT}"
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
