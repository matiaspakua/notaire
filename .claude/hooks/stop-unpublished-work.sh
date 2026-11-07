#!/usr/bin/env bash
# Stop hook: continuous publication (#1681, CONSTITUTION §9/§10).
#
# When the agent is about to end its turn on an Issue branch (`<type>/<issue>_<desc>`) whose
# commits exist only on this disk, block the stop once (exit 2) and say how to publish. A stage
# that only exists locally is not done: another agent or person cannot resume it from the Issue.
#
# Deliberately narrow (see .claude/rules/hooks.md, "Adding a new hook"):
#   - only Issue branches, only commits that are on no remote; uncommitted edits are not judged;
#   - at most once per stop: Claude Code sets stop_hook_active on the retry, and the hook lets it
#     through, so a session that must not push (the user said so) is never trapped;
#   - any error (no git, no payload, detached HEAD) lets the stop through.
set -uo pipefail

payload="$(cat 2>/dev/null || true)"

read -r active cwd < <(printf '%s' "$payload" | python3 -c '
import json, sys
try:
    data = json.load(sys.stdin)
except Exception:
    data = {}
print("1" if data.get("stop_hook_active") else "0", data.get("cwd") or "")
' 2>/dev/null || echo "0 ")

[ "${active:-0}" = "1" ] && exit 0

dir="${cwd:-${CLAUDE_PROJECT_DIR:-.}}"
branch="$(git -C "$dir" rev-parse --abbrev-ref HEAD 2>/dev/null || true)"
[[ "$branch" =~ ^[a-z]+/([0-9]+)_ ]] || exit 0
issue="${BASH_REMATCH[1]}"

local_only="$(git -C "$dir" rev-list --count HEAD --not --remotes 2>/dev/null || echo 0)"
[ "${local_only:-0}" -gt 0 ] 2>/dev/null || exit 0

cat >&2 <<EOF
Continuous publication (#1681): ${local_only} commit(s) on ${branch} exist only on this disk.
Publish them before you stop, so the Issue holds the current state:

  bash workspace/sdlc/publish-progress.sh ${issue} "<stage; what remains>"

If you must not push (the user asked you not to, or the pre-push preflight is red), say so in
your final message, with the reason and the command that resumes the work.
EOF
exit 2
