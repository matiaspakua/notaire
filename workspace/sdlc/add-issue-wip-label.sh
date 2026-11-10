#!/usr/bin/env bash
#
# add-issue-wip-label.sh — Gate 1.6: add the WIP label honestly (#2221 / CU76).
#
# Cloud Cursor integrations often lack Issues write (GraphQL addLabelsToLabelable).
# Prefer notaire-fleet (`eval "$(bash workspace/sdlc/fleet-token.sh)"`) when the
# Owner configured FLEET_APP_ID / FLEET_APP_PRIVATE_KEY. When the ambient token
# cannot label, print an honest SKIPPED note and exit 0 so Gate 1 continues —
# never claim the Issue is "in progress".
#
# USAGE
#   bash workspace/sdlc/add-issue-wip-label.sh <issue-number>
#
# Exit 0 = labeled OR honestly skipped (permission / missing label ACL).
# Exit 1 = usage error or unexpected gh failure.
# Exit 2 = bad arguments.
#
# See docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md

set -euo pipefail

ISSUE="${1:-}"
if [[ ! "$ISSUE" =~ ^[0-9]+$ ]]; then
  echo "Usage: bash workspace/sdlc/add-issue-wip-label.sh <issue-number>" >&2
  exit 2
fi

# Prefer taxonomy name once it exists on the repo (#1860).
resolve_wip_label() {
  local names
  names="$(gh label list --limit 200 --json name -q '.[].name' 2>/dev/null || true)"
  if printf '%s\n' "$names" | grep -qx 'status:in-progress'; then
    printf '%s\n' 'status:in-progress'
  elif printf '%s\n' "$names" | grep -qx 'in-progress'; then
    printf '%s\n' 'in-progress'
  else
    # Prefer the new name; gh may still 404 — callers treat that honestly below.
    printf '%s\n' 'status:in-progress'
  fi
}

try_add() {
  local label="$1"
  local err
  err="$(gh issue edit "$ISSUE" --add-label "$label" 2>&1)" && {
    printf '✓ #%s labeled %s\n' "$ISSUE" "$label"
    return 0
  }
  printf '%s\n' "$err" >&2
  # Permission: GraphQL / REST 403 from Cursor cloud or other integrations.
  if printf '%s' "$err" | grep -qiE \
    'Resource not accessible by integration|addLabelsToLabelable|HTTP[[:space:]]*403|403[[:space:]]*Forbidden|permission'; then
    cat >&2 <<EOF
⚠ Gate 1.6 SKIPPED: integration cannot add issue labels (addLabelsToLabelable /
  issues: write). Do not mark the Issue as in progress. Continue Gate 1.
  Owner: grant Issues Read & write on the Cursor GitHub App / cloud token, or
  configure notaire-fleet (FLEET_APP_ID + FLEET_APP_PRIVATE_KEY) and
  eval "\$(bash workspace/sdlc/fleet-token.sh)".
  Docs: docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md
EOF
    return 10
  fi
  # Wrong legacy/new name — caller may retry the other.
  if printf '%s' "$err" | grep -qiE "not found|'${label}' not found|could not be found"; then
    return 11
  fi
  printf '✗ #%s: unexpected failure adding label %s\n' "$ISSUE" "$label" >&2
  return 1
}

LABEL="$(resolve_wip_label)"
set +e
try_add "$LABEL"
rc=$?
set -e

if [ "$rc" -eq 0 ]; then
  exit 0
fi
if [ "$rc" -eq 10 ]; then
  exit 0
fi

# Retry the other taxonomy name once when the preferred label is missing.
if [ "$rc" -eq 11 ]; then
  ALT="in-progress"
  if [ "$LABEL" = "in-progress" ]; then
    ALT="status:in-progress"
  fi
  set +e
  try_add "$ALT"
  rc=$?
  set -e
  if [ "$rc" -eq 0 ] || [ "$rc" -eq 10 ]; then
    exit 0
  fi
  if [ "$rc" -eq 11 ]; then
    cat >&2 <<EOF
⚠ Gate 1.6 SKIPPED: neither status:in-progress nor in-progress exists (or is
  writable) for this integration. Continue Gate 1 without faking WIP.
  See docs/300-development/305-project-management/LABELS.md
EOF
    exit 0
  fi
fi

exit 1
