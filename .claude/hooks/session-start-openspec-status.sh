#!/usr/bin/env bash
# SessionStart hook: surface in-flight OpenSpec change status when a Claude
# Code session opens in this repo, so the agent doesn't have to remember to
# check (or worse, forget an in-progress change exists).
#
# Informational only — never blocks session start. If the openspec CLI is
# missing or errors, print a one-line fallback instead of failing.
#
# See .claude/rules/hooks.md for rationale and manual test notes.
set -uo pipefail

echo "## OpenSpec change status (Notaire CONSTITUTION.md workflow)"

timeout_cmd=""
if command -v timeout >/dev/null 2>&1; then
    timeout_cmd="timeout 5"
elif command -v gtimeout >/dev/null 2>&1; then
    timeout_cmd="gtimeout 5"
fi

if command -v openspec >/dev/null 2>&1; then
    if ! $timeout_cmd openspec list 2>&1; then
        echo "(openspec list failed or timed out — check 'openspec' CLI installation)"
    fi
else
    echo "(openspec CLI not found on PATH — install it to see in-flight change status)"
fi

echo
echo "Reminder: CONSTITUTION.md at repo root governs every change. No code change"
echo "without a linked GitHub Issue + Use Case + OpenSpec change (Gate 1)."

exit 0
