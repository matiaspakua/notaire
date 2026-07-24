#!/usr/bin/env bash
#
# install-git-hooks.sh — point git at the repo's version-controlled hooks.
#
# Uses core.hooksPath rather than copying files into .git/hooks, so the hooks
# stay under version control, apply to every worktree of this repo, and update
# themselves when someone pulls. Run once per clone:
#
#   bash scripts/install-git-hooks.sh
#
# Uninstall with:  git config --unset core.hooksPath
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    echo "not a git repository: $REPO_ROOT" >&2
    exit 1
fi

chmod +x .githooks/* scripts/preflight.sh scripts/spotless-fallback.sh 2>/dev/null || true
git config core.hooksPath .githooks

cat <<MSG
✓ git hooks installed (core.hooksPath = .githooks)

  pre-push  runs scripts/preflight.sh — the same gates CI runs — and blocks the
            push if any of them fail.

Everyday use:
  bash scripts/preflight.sh --fix    fix what is auto-fixable, then re-check
  bash scripts/preflight.sh          full local gate run
  bash scripts/preflight.sh --list   which local check maps to which CI job

Bypass (deliberately):
  PREFLIGHT_SKIP=1 git push …        skip the gates for one push
  PREFLIGHT_FAST=1 git push …        format/lint/compile/typecheck only

Uninstall:
  git config --unset core.hooksPath
MSG
