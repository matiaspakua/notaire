#!/usr/bin/env bash
#
# apply-protect-main-ruleset.sh — extend GitHub ruleset protect-main (#1040).
#
# Updates repository ruleset id 24128115 to enforce:
#   - pull_request (PR-only merges; no required approving reviews)
#   - required_status_checks: CI, Frontend CI, Playwright E2E, Code Lint,
#     PR Validation (strict)
#   - deletion + non_fast_forward (kept)
#   - bypass_actors empty (after #1041 — no durable Actions bypass)
#
# Does NOT create a second ruleset. Does NOT enable classic branch protection.
#
# Requires admin (administration scope). Integration / fleet tokens get HTTP 403.
#
# USAGE
#   bash security/apply-protect-main-ruleset.sh            # dry-run (default)
#   bash security/apply-protect-main-ruleset.sh --dry-run
#   bash security/apply-protect-main-ruleset.sh --apply
#   bash security/apply-protect-main-ruleset.sh --repo owner/name --apply
set -euo pipefail

REPO="${PROTECT_MAIN_REPO:-matiaspakua/notaire}"
RULESET_ID="${PROTECT_MAIN_RULESET_ID:-24128115}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DESIRED_JSON="${PROTECT_MAIN_DESIRED_JSON:-${SCRIPT_DIR}/rulesets/protect-main.desired.json}"
APPLY=0

usage() {
  sed -n '3,22p' "${BASH_SOURCE[0]}"
}

while [ $# -gt 0 ]; do
  case "$1" in
    --repo)
      REPO="${2:?--repo requires owner/name}"
      shift 2
      ;;
    --apply) APPLY=1; shift ;;
    --dry-run) APPLY=0; shift ;;
    -h|--help) usage; exit 0 ;;
    *)
      echo "unknown option: $1 (try --help)" >&2
      exit 2
      ;;
  esac
done

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is required: https://cli.github.com" >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required" >&2
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "gh is not authenticated. Run: gh auth login (admin PAT required for --apply)" >&2
  exit 1
fi

if [ ! -f "$DESIRED_JSON" ]; then
  echo "desired-state file missing: $DESIRED_JSON" >&2
  exit 1
fi

# PUT body: drop documentation-only id; keep API fields.
PUT_BODY="$(jq -c '{
  name,
  target,
  enforcement,
  bypass_actors: (.bypass_actors // []),
  conditions,
  rules
}' "$DESIRED_JSON")"

echo "=== protect-main ruleset apply (${REPO} id=${RULESET_ID}) ==="
echo "Desired state: ${DESIRED_JSON}"
echo ""
echo "PUT payload:"
echo "$PUT_BODY" | jq .

if [ "$APPLY" = 0 ]; then
  echo ""
  echo "Dry-run only. Re-run with --apply as a repo admin to update ruleset ${RULESET_ID}."
  echo "After apply: bash security/assert-protect-main-ruleset.sh"
  exit 0
fi

echo ""
echo "Applying PUT /repos/${REPO}/rulesets/${RULESET_ID} ..."
echo "$PUT_BODY" | gh api \
  --method PUT \
  -H "Accept: application/vnd.github+json" \
  "repos/${REPO}/rulesets/${RULESET_ID}" \
  --input - \
  --jq '{id,name,enforcement,bypass_actors,rules:[.rules[].type]}'

echo ""
echo "Applied. Run: bash security/assert-protect-main-ruleset.sh"
