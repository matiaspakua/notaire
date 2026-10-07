#!/usr/bin/env bash
#
# check-tdd-evidence.sh — a PR that changes production code shows tests first.
#
# The harness has a red gate; nothing else checked TDD (local-ai/AUDIT.md E2).
# This is the cheap, history-based form: the range must change a test file, and
# no commit may change production code before the first commit that changes a
# test (a commit changing both counts as tests-first). It does not re-run the
# red state. Run by sdlc-process.yml and preflight.sh.
#
# A human-set `sdlc-exception` label (in $PR_LABELS) waives it (CONSTITUTION §12).
#
# USAGE  PR_LABELS="a,b" bash workspace/sdlc/check-tdd-evidence.sh <base> [head]
set -uo pipefail

BASE="${1:?usage: check-tdd-evidence.sh <base> [head]}"
HEAD="${2:-HEAD}"
PROD='^backend-api/src/main/|^frontend/src/'
TEST='/src/test/|\.test\.tsx?$|^testing/e2e/tests/|/__tests__/'

if grep -qw 'sdlc-exception' <<< "${PR_LABELS:-}"; then
  echo "✓ sdlc-exception label set — TDD evidence check waived"
  exit 0
fi

seen_test=0; first_bad=""; any_prod=0
while read -r sha; do
  files="$(git diff-tree --no-commit-id --name-only -r "$sha")"
  touches_test=0; touches_prod=0
  grep -qE "$TEST" <<< "$files" && touches_test=1
  grep -E "$PROD" <<< "$files" | grep -qvE "$TEST" && touches_prod=1
  [ "$touches_test" = 1 ] && seen_test=1
  [ "$touches_prod" = 1 ] && any_prod=1
  if [ "$touches_prod" = 1 ] && [ "$seen_test" = 0 ] && [ -z "$first_bad" ]; then
    first_bad="$(git log -1 --format='%h %s' "$sha")"
  fi
done < <(git rev-list --reverse --no-merges "$BASE..$HEAD")

if [ "$any_prod" = 0 ]; then
  echo "✓ no production code changed"
  exit 0
fi
if [ "$seen_test" = 0 ]; then
  echo "✗ production code changed but no test file did (CONSTITUTION §7: tests first)"
  exit 1
fi
if [ -n "$first_bad" ]; then
  echo "✗ production code changed before any test: $first_bad"
  echo "  Commit the failing test first (red), then the implementation (green)."
  exit 1
fi
echo "✓ tests come before production code"
