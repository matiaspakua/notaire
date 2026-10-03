#!/usr/bin/env bash
#
# assert-protect-main-ruleset.sh — read-only Gate 5 check for #1040.
#
# Fetches live ruleset protect-main (id 24128115) and verifies:
#   - pull_request + required_status_checks + deletion + non_fast_forward
#   - required check contexts exactly match the five AC names
#   - bypass_actors empty
#
# USAGE
#   bash scripts/assert-protect-main-ruleset.sh
#   bash scripts/assert-protect-main-ruleset.sh --repo owner/name
set -euo pipefail

REPO="${PROTECT_MAIN_REPO:-matiaspakua/notaire}"
RULESET_ID="${PROTECT_MAIN_RULESET_ID:-24128115}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DESIRED_JSON="${PROTECT_MAIN_DESIRED_JSON:-${SCRIPT_DIR}/rulesets/protect-main.desired.json}"

EXPECTED_CHECKS=("CI" "Frontend CI" "Playwright E2E" "Code Lint" "PR Validation")

while [ $# -gt 0 ]; do
  case "$1" in
    --repo)
      REPO="${2:?--repo requires owner/name}"
      shift 2
      ;;
    -h|--help)
      sed -n '3,14p' "${BASH_SOURCE[0]}"
      exit 0
      ;;
    *)
      echo "unknown option: $1" >&2
      exit 2
      ;;
  esac
done

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is required" >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required" >&2
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "gh is not authenticated" >&2
  exit 1
fi

LIVE="$(gh api -H "Accept: application/vnd.github+json" "repos/${REPO}/rulesets/${RULESET_ID}")"
FAIL=0

echo "=== assert protect-main (${REPO} id=${RULESET_ID}) ==="

name="$(echo "$LIVE" | jq -r '.name')"
enforcement="$(echo "$LIVE" | jq -r '.enforcement')"
if [ "$name" != "protect-main" ]; then
  echo "FAIL: name=$name (expected protect-main)" >&2
  FAIL=1
else
  echo "OK name=protect-main"
fi

if [ "$enforcement" != "active" ]; then
  echo "FAIL: enforcement=$enforcement (expected active)" >&2
  FAIL=1
else
  echo "OK enforcement=active"
fi

types="$(echo "$LIVE" | jq -r '[.rules[].type] | sort | join(",")')"
for required in deletion non_fast_forward pull_request required_status_checks; do
  if ! echo "$types" | tr ',' '\n' | grep -qx "$required"; then
    echo "FAIL: missing rule type: $required (have: $types)" >&2
    FAIL=1
  else
    echo "OK rule=$required"
  fi
done

live_checks="$(echo "$LIVE" | jq -r '
  [.rules[]
    | select(.type=="required_status_checks")
    | .parameters.required_status_checks[]?
    | .context] | sort | .[]
')"
expected_sorted="$(printf '%s\n' "${EXPECTED_CHECKS[@]}" | sort)"
if [ "$(echo "$live_checks")" != "$(echo "$expected_sorted")" ]; then
  echo "FAIL: required checks mismatch" >&2
  echo "  live:" >&2
  echo "$live_checks" | sed 's/^/    /' >&2
  echo "  expected:" >&2
  echo "$expected_sorted" | sed 's/^/    /' >&2
  FAIL=1
else
  echo "OK required_status_checks=${EXPECTED_CHECKS[*]}"
fi

bypass_count="$(echo "$LIVE" | jq '
  if .bypass_actors == null then 0 else (.bypass_actors | length) end
')"
if [ "$bypass_count" != "0" ]; then
  echo "FAIL: bypass_actors not empty (count=$bypass_count)" >&2
  echo "$LIVE" | jq '.bypass_actors' >&2
  FAIL=1
else
  echo "OK bypass_actors empty"
fi

include="$(echo "$LIVE" | jq -r '.conditions.ref_name.include[]?' )"
if ! echo "$include" | grep -qx '~DEFAULT_BRANCH'; then
  echo "FAIL: conditions.ref_name.include missing ~DEFAULT_BRANCH" >&2
  FAIL=1
else
  echo "OK target=~DEFAULT_BRANCH"
fi

pr_reviews="$(echo "$LIVE" | jq -r '
  [.rules[] | select(.type=="pull_request") | .parameters.required_approving_review_count // 0][0] // 0
')"
if [ "$pr_reviews" != "0" ]; then
  echo "FAIL: required_approving_review_count=$pr_reviews (expected 0 for fleet merge)" >&2
  FAIL=1
else
  echo "OK required_approving_review_count=0"
fi

if [ -f "$DESIRED_JSON" ]; then
  desired_id="$(jq -r '.id' "$DESIRED_JSON")"
  if [ "$desired_id" != "$RULESET_ID" ]; then
    echo "FAIL: desired JSON id=$desired_id != RULESET_ID=$RULESET_ID" >&2
    FAIL=1
  else
    echo "OK desired JSON id matches"
  fi
fi

if [ "$FAIL" -ne 0 ]; then
  echo ""
  echo "ASSERT FAILED. Apply with: bash scripts/apply-protect-main-ruleset.sh --apply" >&2
  exit 1
fi

echo ""
echo "ASSERT OK — protect-main matches #1040 desired policy."
