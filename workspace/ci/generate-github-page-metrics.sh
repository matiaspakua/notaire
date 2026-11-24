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

# Coverage floors as enforced by the builds, and the Owner's target (#2046, #2475): never typed in the page.
pct() { awk -v v="$1" 'BEGIN { printf "%d", v * 100 + 0.5 }'; }
FLOORS=$(grep -o '<minimum>0\.[0-9]*</minimum>' backend-api/pom.xml | grep -o '0\.[0-9]*' | head -2 | tr '\n' ' ')
BACKEND_LINE_FLOOR=$(pct "$(echo "$FLOORS" | cut -d' ' -f1)")
BACKEND_BRANCH_FLOOR=$(pct "$(echo "$FLOORS" | cut -d' ' -f2)")
FRONTEND_LINES_FLOOR=$(grep -o 'lines: *[0-9]*' frontend/vitest.config.ts | head -1 | grep -o '[0-9]*$')
FRONTEND_BRANCHES_FLOOR=$(grep -o 'branches: *[0-9]*' frontend/vitest.config.ts | head -1 | grep -o '[0-9]*$')
COVERAGE_TARGET=90  # Owner target >90% across the pyramid (#2046, .claude/rules/code-quality.md)

# AI harnesses configured in this repository (#2475): one per configuration file an agent reads.
AI_TOOL_NAMES=()
[ -f CLAUDE.md ] && AI_TOOL_NAMES+=("Claude Code")
[ -d .codex ] && AI_TOOL_NAMES+=("Codex")
[ -d .cursor ] && AI_TOOL_NAMES+=("Cursor")
[ -d .junie ] && AI_TOOL_NAMES+=("Junie")
[ -f opencode.json ] && AI_TOOL_NAMES+=("OpenCode")
[ -f .github/copilot-instructions.md ] && AI_TOOL_NAMES+=("GitHub Copilot")
AI_TOOLS_JSON=$(printf '%s\n' "${AI_TOOL_NAMES[@]}" | jq -R . | jq -cs .)

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

# -----------------------------------------------------------------------
# Issue #760 — compute e2eTests from Playwright results, apiEndpoints from
# Bruno report.  Fall back to last live value only when the artefact is
# missing, and visibly mark the fallback stale.
# -----------------------------------------------------------------------

# Sets VALUE to the fresh count read from JSON file $1 with jq filter $3, or else to the last live
# value of key $2; STALE is true only when it fell back to a live value (#760).
fresh_or_live() {
  local file="$1" key="$2" filter="$3" fresh=""
  if [ -n "$file" ] && [ -f "$file" ]; then
    fresh=$(jq -r "$filter" "$file" 2>/dev/null || true)
  fi
  if [ -n "$fresh" ] && [ "$fresh" != "null" ] && [ "$fresh" != "0" ]; then
    VALUE="$fresh"; STALE=false
    return
  fi
  VALUE=$(coverage_fallback "$key")
  case "$VALUE" in ""|null) VALUE=null; STALE=false ;; *) STALE=true ;; esac
}

fresh_or_live "${PLAYWRIGHT_RESULTS_JSON:-}" e2eTests '.stats.total // empty'
E2E_TESTS="$VALUE"; E2E_TESTS_STALE="$STALE"
# Bruno CLI --reporter-json writes a list of per-iteration objects, each with summary.totalRequests (#658).
fresh_or_live "${BRUNO_RESULTS_JSON:-}" apiEndpoints \
  'if type == "array" then (map(.summary.totalRequests // 0) | add // 0) else (.summary.totalRequests // empty) end'
API_ENDPOINTS="$VALUE"; API_ENDPOINTS_STALE="$STALE"

# Build the coverage JSON object.  Stale flags are only emitted when the
# value is a non-null fallback (issue #760).
_COVERAGE_BUILD='
  {backendInstruction:$bi, backendBranch:$bb, frontendComponent:$fc,
   e2eTests:$et, apiEndpoints:$ae, lastUpdated:$lu}
  | (if $et_stale == "true" then . + {e2eTestsStale: true} else . end)
  | (if $ae_stale == "true" then . + {apiEndpointsStale: true} else . end)
'
COVERAGE_JSON=$(jq -n \
  --argjson bi "$BACKEND_INSTRUCTION" \
  --argjson bb "$BACKEND_BRANCH" \
  --argjson fc "$FRONTEND_COMPONENT" \
  --argjson et "$E2E_TESTS" \
  --argjson ae "$API_ENDPOINTS" \
  --arg lu "$COVERAGE_UPDATED" \
  --arg et_stale "$E2E_TESTS_STALE" \
  --arg ae_stale "$API_ENDPOINTS_STALE" \
  "$_COVERAGE_BUILD")

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
  printf '  "aiTools": %s,\n' "${#AI_TOOL_NAMES[@]}"
  printf '  "aiToolNames": %s,\n' "${AI_TOOLS_JSON}"
  printf '  "coverageFloors": {"backendLine": %s, "backendBranch": %s, "frontendLines": %s, "frontendBranches": %s},\n' \
    "${BACKEND_LINE_FLOOR}" "${BACKEND_BRANCH_FLOOR}" "${FRONTEND_LINES_FLOOR}" "${FRONTEND_BRANCHES_FLOOR}"
  printf '  "coverageTarget": %s,\n' "${COVERAGE_TARGET}"
  printf '  "contributors": %s,\n' "${CONTRIBUTORS}"
  printf '  "firstCommitDate": "%s",\n' "${FIRST_COMMIT}"
  printf '  "coverage": %s\n' "${COVERAGE_JSON}"
  printf '%s\n' '}'
} > "$OUTPUT_FILE"

echo "=== metrics.json ==="
cat "$OUTPUT_FILE"
