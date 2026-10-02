#!/usr/bin/env bash
#
# check-heavy-ci.sh — refuse merge on light-only green PRs.
#
# Exit 0 only when Integration, Coverage Gate, Bruno, and Playwright are all
# success for the PR head. Docker is optional. Used by fleet agents before
# `gh pr merge`. See docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md.
#
# USAGE  bash scripts/check-heavy-ci.sh <pr-number>
set -euo pipefail

PR="${1:?usage: check-heavy-ci.sh <pr-number>}"

if ! command -v gh >/dev/null 2>&1; then
  echo "✗ gh CLI required" >&2
  exit 2
fi

REQUIRED=(
  "Integration Tests"
  "Coverage Gate (mvn verify)"
  "API Tests (Bruno)"
  "UI E2E Tests (Playwright)"
)

# Tab-separated: name, state, ...
checks="$(gh pr checks "$PR" 2>/dev/null || true)"
if [[ -z "$checks" ]]; then
  echo "✗ no checks returned for PR #$PR (queued or inaccessible)" >&2
  exit 1
fi

status_of() {
  local name="$1"
  # Exact name match in column 1
  awk -F'\t' -v n="$name" '$1 == n { print $2; found=1 } END { if (!found) print "missing" }' <<<"$checks"
}

fail=0
echo "Heavy CI gate for PR #$PR:"
for name in "${REQUIRED[@]}"; do
  st="$(status_of "$name")"
  case "$st" in
    pass|success)
      echo "  ✓ $name ($st)"
      ;;
    *)
      echo "  ✗ $name ($st)" >&2
      fail=1
      ;;
  esac
done

if [[ "$fail" -ne 0 ]]; then
  echo "✗ not merge-ready — wait for Integration + Coverage + Bruno + Playwright" >&2
  echo "  (light checks alone are insufficient; see CI-MERGE-GATE.md)" >&2
  exit 1
fi

echo "✓ heavy CI green — safe to consider merge (still verify head SHA)"
exit 0
