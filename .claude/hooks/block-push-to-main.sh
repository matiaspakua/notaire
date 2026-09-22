#!/usr/bin/env bash
# PreToolUse hook for the Bash tool.
#
# Defense in depth against pushing directly to main/master: GitHub branch
# protection is NOT currently configured on this repo's `main` branch
# (verified via `gh api repos/matiaspakua/notaire/branches/main/protection`
# -> 404 "Branch not protected"), so this is the only guard against an
# accidental `git push origin main` today.
#
# Reads the tool call payload as JSON on stdin (Claude Code PreToolUse
# contract), inspects tool_input.command for a `git push` invocation, and
# blocks (exit 2, message on stderr) when the resolved target branch is
# exactly "main" or "master". Any other command, or a push to any other
# branch, is allowed through unchanged (exit 0).
#
# See .claude/rules/hooks.md for rationale and manual test notes.
set -euo pipefail

payload="$(cat)"

command=$(printf '%s' "$payload" | python3 -c '
import json, sys
try:
    data = json.load(sys.stdin)
    print(data.get("tool_input", {}).get("command", ""))
except Exception:
    print("")
')

# Only look at commands that actually invoke git push.
if ! printf '%s' "$command" | grep -qE '(^|[;&|]|\bgit\s.*-C\s+\S+\s+)git\s+push\b'; then
    exit 0
fi

# Explicit --force / --force-with-lease to a non-main branch is out of scope
# here (see proposal.md — Out of Scope); we only look at the destination branch.

target=""

# git push <remote> <refspec>  (e.g. "git push origin main", "git push origin HEAD:main")
if printf '%s' "$command" | grep -qE '\bgit\s+push\b\s+\S+\s+\S+'; then
    refspec=$(printf '%s' "$command" | grep -oE 'git\s+push\s+\S+\s+\S+' | awk '{print $NF}')
    # Strip a local:remote refspec down to the remote side.
    target="${refspec##*:}"
fi

# No explicit refspec ("git push" / "git push origin" / "git push -u origin"):
# resolve the branch git would actually push, i.e. the current branch.
if [ -z "$target" ] || [ "$target" = "HEAD" ]; then
    target=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")
fi

if [ "$target" = "main" ] || [ "$target" = "master" ]; then
    cat >&2 <<EOF
Blocked: direct 'git push' to '$target' is not allowed in this repository.

CONSTITUTION.md requires all changes to land via Pull Request (never push to
main/master directly). Open a PR instead:

  git push -u origin <your-feature-branch>
  gh pr create --title "..." --body "..."

(This hook is defense in depth: GitHub branch protection is not currently
configured on 'main' in this repo.)
EOF
    exit 2
fi

exit 0
