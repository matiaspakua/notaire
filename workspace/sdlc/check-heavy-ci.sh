#!/usr/bin/env bash
#
# check-heavy-ci.sh — heavy-suite status for a pull request.
#
# Product CI does not run on pull_request (#2001). The merge gate is
# `bash workspace/sdlc/preflight.sh --full` locally. This script still fails
# when a heavy workflow was started on the PR head and is pending or red.
# Missing checks with no workflow run are success: GitHub will not report them.
# The Owner must drop retired required checks from protect-main (#1040) or
# GitHub still refuses the merge. See CI-MERGE-GATE.md.
#
# USAGE  bash workspace/sdlc/check-heavy-ci.sh <pr-number>
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
    # Path-scoped CI (#1257): docs/OpenSpec PRs intentionally skip Java/E2E leaves;
    # suite aggregators report skipped; `gh pr checks` often prints "skipping".
    skip|skipped|skipping)
      echo "  ✓ $name ($st — path-scoped skip)"
      ;;
    missing)
      wf="$(workflow_for "$name")"
      wst="$(workflow_status "$wf")"
      case "$wst" in
        queued|pending|in_progress|waiting|requested)
          echo "  ✗ $name (pending — workflow ${wf}: ${wst})" >&2
          fail=1
          ;;
        failure|cancelled|timed_out|startup_failure|stale)
          echo "  ✗ $name (workflow ${wf}: ${wst})" >&2
          fail=1
          ;;
        "")
          echo "  ✓ $name (not run on pull requests — local preflight --full is the gate)"
          ;;
        *)
          echo "  ✗ $name (workflow ${wf}: ${wst})" >&2
          fail=1
          ;;
      esac
      ;;
    *)
      echo "  ✗ $name ($st)" >&2
      fail=1
      ;;
  esac
done

if [[ "$fail" -ne 0 ]]; then
  echo "✗ not merge-ready — a heavy workflow on this head is pending or failed" >&2
  echo "  Local gate: bash workspace/sdlc/preflight.sh --full (see CI-MERGE-GATE.md)" >&2
  exit 1
fi

if [[ -z "$checks" && -z "$runs" ]]; then
  echo "✓ no GitHub heavy CI on PR #$PR (suites run on version tags, not on pull requests)"
  echo "  Local gate: bash workspace/sdlc/preflight.sh --full"
  echo "  If protect-main still requires CI, Frontend CI, Playwright E2E, Code Lint, or PR Validation, the Owner must update the ruleset (#1040)."
  exit 0
fi

echo "✓ heavy-suite gate clear — local preflight --full remains the pull-request proof"
exit 0
