#!/usr/bin/env bash
#
# check-sdlc-exception.sh — a PR without an OpenSpec change needs an approved exception.
#
# CONSTITUTION §12 allows docs/chore work without a change folder only with
# explicit human approval; nothing recorded that approval (local-ai/AUDIT.md S1).
# The record is now the `sdlc-exception` label, which only a human sets. Dependency
# bots are exempt. Run by sdlc-process.yml and preflight.sh.
#
# USAGE  PR_LABELS="a,b" PR_AUTHOR=login bash workspace/sdlc/check-sdlc-exception.sh <base> [head]
set -uo pipefail

BASE="${1:?usage: check-sdlc-exception.sh <base> [head]}"
HEAD="${2:-HEAD}"

# pull_request workflows check out refs/pull/N/merge. That commit's first parent
# is the base the PR was actually merged into, which can be newer than the
# event's base SHA. Diffing from the stale SHA counts files that landed on main
# (including someone else's docs/openspec/changes/ path) as if this PR added them.
if git rev-parse --verify --quiet "${HEAD}^2" >/dev/null 2>&1; then
  BASE="$(git rev-parse "${HEAD}^1")"
fi

# Capture first: `grep -q` closing the pipe early can SIGPIPE `git diff` and flip the result under pipefail.
CHANGED="$(git diff --name-only "$BASE...$HEAD")"

if grep -q '^docs/openspec/changes/' <<< "$CHANGED"; then
  echo "✓ PR carries an OpenSpec change"
elif grep -qw 'sdlc-exception' <<< "${PR_LABELS:-}"; then
  echo "✓ no OpenSpec change; sdlc-exception label set by a human"
elif grep -qE '^(dependabot|renovate)\[bot\]$' <<< "${PR_AUTHOR:-}"; then
  echo "✓ dependency bot PR — exempt"
else
  echo "✗ no docs/openspec/changes/ path in this PR and no 'sdlc-exception' label."
  echo "  CONSTITUTION §12: work without an OpenSpec change needs explicit human approval."
  echo "  Either add the change folder (openspec new change <name>) or ask the owner to add the label."
  exit 1
fi
