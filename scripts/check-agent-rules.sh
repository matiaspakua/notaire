#!/usr/bin/env bash
#
# check-agent-rules.sh — always-loaded agent rules are non-empty and point at real paths.
#
# Rule files are the highest-leverage prompt every agent reads. One was truncated
# to 0 bytes on main, and a rewrite cited folders that do not exist
# (local-ai/AUDIT.md P2, P5). This checks every backticked repo path in them.
# Paths with placeholders (<name>, {x}, *, ...) and branch names are skipped.
# Run by sdlc-process.yml and preflight.sh.
#
# USAGE  bash scripts/check-agent-rules.sh [repo-root]
set -uo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)}"
TOPS='docs|scripts|\.claude|\.github|backend-api|frontend|notaire-shared|openspec|local-ai|infra|testing'

files=()
for f in "$ROOT"/.claude/rules/*.md "$ROOT"/CLAUDE.md "$ROOT"/AGENTS.md "$ROOT"/CONSTITUTION.md; do
  [ -e "$f" ] && files+=("$f")
done
if [ "${#files[@]}" -eq 0 ]; then
  echo "✗ no agent rule files under $ROOT (usage: check-agent-rules.sh [repo-root])"
  exit 1
fi
bad=0
for f in "${files[@]}"; do
  rel="${f#"$ROOT"/}"
  if [ ! -s "$f" ]; then
    echo "✗ $rel is empty"
    bad=$((bad + 1)); continue
  fi
  while read -r path; do
    [[ "$path" =~ [\<\>\{\}\*\$] || "$path" == *...* ]] && continue
    [[ "$path" =~ ^[a-z]+/[0-9]+_ ]] && continue   # a branch name (docs/257_readme), not a path
    path="${path%%:[0-9]*}"; path="${path%%#*}"
    [ -e "$ROOT/$path" ] || { echo "✗ $rel references missing path: $path"; bad=$((bad + 1)); }
  done < <(grep -oE "\`($TOPS)/[^\`[:space:]]*\`" "$f" | tr -d '`' | sort -u)
done

if [ "$bad" -gt 0 ]; then
  echo "$bad problem(s) in agent rule files"
  exit 1
fi
echo "✓ ${#files[@]} agent rule file(s) are non-empty and reference existing paths"
