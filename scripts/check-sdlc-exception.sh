#!/usr/bin/env bash
#
# check-sdlc-exception.sh — a PR without an OpenSpec change needs an approved exception.
#
# CONSTITUTION §12 allows docs/chore work without a change folder only with
# explicit human approval; nothing recorded that approval (local-ai/AUDIT.md S1).
# The record is now the `sdlc-exception` label, which only a human sets. Dependency
# bots are exempt. Run by sdlc-process.yml and preflight.sh.
#
# USAGE  PR_LABELS="a,b" PR_AUTHOR=login bash scripts/check-sdlc-exception.sh <base> [head]
set -uo pipefail

BASE="${1:?usage: check-sdlc-exception.sh <base> [head]}"
HEAD="${2:-HEAD}"

if git diff --name-only "$BASE...$HEAD" | grep -q '^openspec/changes/'; then
  echo "✓ PR carries an OpenSpec change"
elif grep -qw 'sdlc-exception' <<< "${PR_LABELS:-}"; then
  echo "✓ no OpenSpec change; sdlc-exception label set by a human"
elif grep -qE '^(dependabot|renovate)\[bot\]$' <<< "${PR_AUTHOR:-}"; then
  echo "✓ dependency bot PR — exempt"
else
  echo "✗ no openspec/changes/ path in this PR and no 'sdlc-exception' label."
  echo "  CONSTITUTION §12: work without an OpenSpec change needs explicit human approval."
  echo "  Either add the change folder (openspec new change <name>) or ask the owner to add the label."
  exit 1
fi
