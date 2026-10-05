#!/usr/bin/env bash
#
# check-agent-rules.sh — always-loaded agent rules are non-empty and point at real paths.
#
# Rule files are the highest-leverage prompt every agent reads. One was truncated
# to 0 bytes on main, and a rewrite cited folders that do not exist
# (local-ai/AUDIT.md P2, P5). This checks every backticked repo path in them.
# Paths with placeholders (<name>, {x}, *, ...) and branch names are skipped.
# Also rejects obsolete migration-era targets in .claude/rules/refactoring.md (#1070).
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

# refactoring.md must describe the current stack, not the 2025 Swing migration target (#1070).
refactoring="$ROOT/.claude/rules/refactoring.md"
if [ -f "$refactoring" ]; then
  while IFS= read -r pat; do
    if grep -qE -- "$pat" "$refactoring"; then
      echo "✗ .claude/rules/refactoring.md contains obsolete marker matching /$pat/ (see #1070)"
      bad=$((bad + 1))
    fi
  done <<'OBSOLETE'
com\.notaria
Spring Boot 3
Java 17
PostgreSQL 15
EntityRequestDTO
EntityResponseDTO
SwingWorker
JOptionPane
standalone Swing GUI client
OBSOLETE
  while IFS= read -r pat; do
    if ! grep -qE -- "$pat" "$refactoring"; then
      echo "✗ .claude/rules/refactoring.md missing current-stack marker matching /$pat/ (see #1070)"
      bad=$((bad + 1))
    fi
  done <<'CURRENT'
com\.licensis\.notaire
Spring Boot 4
Java 26
PostgreSQL 16
Next\.js
Dto[A-Z]
CURRENT
fi

if [ "$bad" -gt 0 ]; then
  echo "$bad problem(s) in agent rule files"
  exit 1
fi
echo "✓ ${#files[@]} agent rule file(s) are non-empty and reference existing paths"
