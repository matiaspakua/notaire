#!/usr/bin/env bash
#
# check-heavy-ci.sh — refuse merge on light-only green PRs.
#
# Exit 0 only when Integration, Coverage Gate, Bruno, and Playwright are all
# success for the PR head. Docker is optional. Used by fleet agents before
# `gh pr merge`. See docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md.
#
# When a required check is missing from `gh pr checks`, also inspect
# `gh run list` for the parent heavy workflows so queued/pending runs are
# reported as pending (not bare missing) — still fails the gate.
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

WF_CI="CI - Build, Test & Security"
WF_E2E="Playwright E2E — Full Suite"

workflow_for() {
  case "$1" in
    "Integration Tests"|"Coverage Gate (mvn verify)") echo "$WF_CI" ;;
    "API Tests (Bruno)"|"UI E2E Tests (Playwright)") echo "$WF_E2E" ;;
  esac
}

# Tab-separated: name, state, ...
checks="$(gh pr checks "$PR" 2>/dev/null || true)"
if [[ -z "$checks" ]]; then
  echo "✗ no checks returned for PR #$PR (queued or inaccessible)" >&2
  exit 1
fi

pr_meta="$(gh pr view "$PR" --json headRefName,headRefOid -q '.headRefName + "\t" + .headRefOid')"
branch="${pr_meta%%$'\t'*}"
head_sha="${pr_meta#*$'\t'}"

# name\tstatus for heavy workflows on this head
runs="$(
  gh run list --branch "$branch" --commit "$head_sha" --limit 20 \
    --json name,status \
    --jq ".[] | select(.name == \"$WF_CI\" or .name == \"$WF_E2E\") | \"\\(.name)\\t\\(.status)\"" \
    2>/dev/null || true
)"

status_of() {
  local name="$1"
  awk -F'\t' -v n="$name" '$1 == n { print $2; found=1 } END { if (!found) print "missing" }' <<<"$checks"
}

workflow_status() {
  local wf="$1"
  awk -F'\t' -v n="$wf" '$1 == n { print $2; exit }' <<<"$runs"
}

fail=0
echo "Heavy CI gate for PR #$PR (head ${head_sha:0:7}):"
for name in "${REQUIRED[@]}"; do
  st="$(status_of "$name")"
  case "$st" in
    pass|success)
      echo "  ✓ $name ($st)"
      ;;
    missing)
      wf="$(workflow_for "$name")"
      wst="$(workflow_status "$wf")"
      case "$wst" in
        queued|pending|in_progress|waiting|requested)
          echo "  ✗ $name (pending — workflow ${wf}: ${wst})" >&2
          ;;
        *)
          echo "  ✗ $name (missing)" >&2
          ;;
      esac
      fail=1
      ;;
    *)
      echo "  ✗ $name ($st)" >&2
      fail=1
      ;;
  esac
done

if [[ "$fail" -ne 0 ]]; then
  echo "✗ not merge-ready — wait for Integration + Coverage + Bruno + Playwright" >&2
  echo "  (light checks / subscription success alone are insufficient; see CI-MERGE-GATE.md)" >&2
  exit 1
fi

echo "✓ heavy CI green — safe to consider merge (still verify head SHA)"
exit 0
