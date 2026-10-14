#!/usr/bin/env bash
#
# publish-progress.sh — continuous publication of agent work (CONSTITUTION §9, #1681).
#
# Pushes the current branch, confirms the remote head equals the local head, then comments
# on the issue with the branch, last commit and stage so another person or agent can resume.
#
# USAGE
#   bash workspace/sdlc/publish-progress.sh <issue-number> "<stage reached; what remains>"
#
# Run it right after creating a branch (stage: "branch created") and after each group of
# atomic commits (spec, failing tests, implementation, docs). A rejected push (for example
# by the pre-push preflight) exits non-zero and posts no comment: the stage is not published.

set -euo pipefail

ISSUE="${1:-}"
STAGE="${2:-}"

if [[ ! "$ISSUE" =~ ^[0-9]+$ ]] || [ -z "$STAGE" ]; then
  echo 'usage: publish-progress.sh <issue-number> "<stage reached; what remains>"' >&2
  exit 2
fi

BRANCH="$(git branch --show-current)"
case "$BRANCH" in
  ""|main|master)
    echo "refusing to publish from '${BRANCH:-detached HEAD}': work on a <type>/<issue>_<desc> branch" >&2
    exit 1
    ;;
esac

git push -u origin "$BRANCH"

LOCAL_HEAD="$(git rev-parse HEAD)"
REMOTE_HEAD="$(git ls-remote --heads origin "$BRANCH" | awk '{print $1}')"
if [ "$LOCAL_HEAD" != "$REMOTE_HEAD" ]; then
  echo "remote head (${REMOTE_HEAD:-none}) does not match local HEAD $LOCAL_HEAD; not commenting" >&2
  exit 1
fi

DIRTY="clean"
[ -n "$(git status --porcelain)" ] && DIRTY="uncommitted changes present (not published)"

gh issue comment "$ISSUE" --body-file - <<BODY
**Progress** — branch \`$BRANCH\`

- Stage: $STAGE
- Last commit: \`$(git log -1 --format='%h %s')\`
- Working tree: $DIRTY
- Resume: \`git fetch origin && git checkout $BRANCH\`
BODY

echo "published $BRANCH @ ${LOCAL_HEAD:0:10} and commented on #$ISSUE"
