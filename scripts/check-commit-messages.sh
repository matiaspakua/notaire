#!/usr/bin/env bash
#
# check-commit-messages.sh — every commit on the PR is a Conventional Commit.
#
# pr-validation.yml checks only the PR title; a `feat(test)`-style or free-text
# commit subject still merged (local-ai/AUDIT.md E3). Merge commits are skipped.
# Run by sdlc-process.yml and preflight.sh.
#
# USAGE  bash scripts/check-commit-messages.sh <base> [head]
set -uo pipefail

BASE="${1:?usage: check-commit-messages.sh <base> [head]}"
HEAD="${2:-HEAD}"
TYPES='feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert|design'
# Squash merges carry the PR title: `[#<issue>] <type>(<scope>): <description> (#<pr>)` (CONSTITUTION §4).
PATTERN="^(\[#[0-9]+\] )?((${TYPES})(\([^()]+\))?!?: .+|Revert \".+\")$"

bad=0
while IFS=$'\t' read -r sha subject; do
  [ -n "$sha" ] || continue
  if ! grep -qE "$PATTERN" <<< "$subject"; then
    echo "✗ $sha $subject"
    bad=$((bad + 1))
  fi
done < <(git log --no-merges --format='%h%x09%s' "$BASE..$HEAD")

if [ "$bad" -gt 0 ]; then
  echo "$bad commit subject(s) are not Conventional Commits: <type>(<scope>)?!?: <description>"
  echo "Types: ${TYPES//|/, }. Reword with: git rebase -i $BASE (CONSTITUTION §9)"
  exit 1
fi
echo "✓ all commit subjects are Conventional Commits"
