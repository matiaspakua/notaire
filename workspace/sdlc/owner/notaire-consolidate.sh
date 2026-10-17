#!/usr/bin/env bash
# notaire-consolidate.sh — consolidate milestones, labels and board views (#1847, #1860).
#
#   cd <notaire checkout on an up-to-date main>      # PR #1874 merged is best, not required
#   bash workspace/sdlc/owner/notaire-consolidate.sh           # preview (default): changes nothing
#   bash workspace/sdlc/owner/notaire-consolidate.sh --apply   # performs it
#
# Idempotent: every step reads live state first; a finished run previews "nothing to do".
#   1. labels   — legacy label names left over? (re-runs notaire-labels-apply.sh if present)
#   2. milestones + issues + views — apply_milestone_policy.py:
#        create Catalog / H0 / v0.1 / v0.2, sync history descriptions + due dates,
#        move ~500 issues, project dates, rename/create the 9 views with filters + fields
#   3. retired milestones — delete the empty History buckets merged into H8 (#13, #14)
#   4. stale views — delete "Casos de Uso" and "RNF" (replaced by Catalog)
#   5. board Status — CLOSED issues whose Status is not Done -> Done
#   6. verify — apply_milestone_policy.py --check, coherence --scope wip
# Needs gh logged in as the Owner with repo + project scopes (gh auth refresh -s project).
set -euo pipefail

REPO=matiaspakua/notaire
OWNER=matiaspakua
PROJECT=1
APPLY=0
case "${1:-}" in
  --apply) APPLY=1 ;;
  ""|--preview) APPLY=0 ;;
  *) echo "usage: $0 [--preview|--apply]" >&2; exit 2 ;;
esac
MODE=$([ "$APPLY" = 1 ] && echo APPLY || echo PREVIEW)
say() { if [ "$APPLY" = 1 ]; then echo "  do: $*"; else echo "  would: $*"; fi; }

if ! command -v gh >/dev/null || ! command -v jq >/dev/null; then
  echo "gh and jq are required" >&2
  exit 1
fi
gh auth status >/dev/null 2>&1 || { echo "gh is not logged in" >&2; exit 1; }
[ -f workspace/sdlc/apply_milestone_policy.py ] || { echo "run from the notaire checkout root (main)" >&2; exit 1; }
git fetch -q origin main 2>/dev/null || true
if [ "$(git rev-parse HEAD)" != "$(git rev-parse origin/main 2>/dev/null || git rev-parse HEAD)" ]; then
  echo "! checkout is not origin/main; run: git checkout main && git pull" >&2; exit 1
fi
echo "== notaire consolidate ($MODE)"

echo "== 1. labels"
legacy="$(gh api --paginate "repos/$REPO/labels?per_page=100" -q '.[].name' | grep -xE 'BACKEND|FRONTEND|DB|DEVOPS|TEST|DOC|enhancement|bug|REFACTOR|chore|TAREA|ci|roadmap|in-progress|ready-for-dev|in-review|blocked|needs-info|security|a11y|i18n|ux|risk|tech-debt|requerimiento-funcional|requerimiento-no-funcional|CASO-DE-USO|audit-2026-09|sdlc-exception|phase:.*|Cursor|duplicate|invalid|question|good first issue|help wanted' || true)"
if [ -z "$legacy" ]; then echo "  ok: taxonomy applied, no legacy labels"
elif [ -x workspace/sdlc/owner/notaire-labels-apply.sh ] || [ -f workspace/sdlc/owner/notaire-labels-apply.sh ]; then
  echo "  legacy labels left: $legacy"
  if [ "$APPLY" = 1 ]; then
    bash workspace/sdlc/owner/notaire-labels-apply.sh --apply
  else
    bash workspace/sdlc/owner/notaire-labels-apply.sh
  fi
else echo "  ! legacy labels left: $legacy — run notaire-labels-apply.sh"; fi

echo "== 2. milestones, issue placement, project dates, views"
if [ "$APPLY" = 1 ]; then
  python3 workspace/sdlc/apply_milestone_policy.py --apply
else
  python3 workspace/sdlc/apply_milestone_policy.py --dry-run
fi

echo "== 3. retired milestones"
gh api "repos/$REPO/milestones?state=all&per_page=100" \
  -q '.[]|select(.title=="History · v1.1 priority bucket" or .title=="History · Backlog priority bucket" or .title=="History · v1.0 priority bucket")|"\(.number)\t\(.open_issues+.closed_issues)\t\(.title)"' |
while IFS=$'\t' read -r num count title; do
  [ -n "$num" ] || continue
  if [ "$count" != 0 ]; then
    if [ "$APPLY" = 1 ]; then echo "  ! #$num '$title' still has $count issue(s); re-run after step 2"; else echo "  would: delete milestone #$num '$title' once step 2 has emptied it ($count now)"; fi
    continue
  fi
  say "delete empty milestone #$num '$title'"
  if [ "$APPLY" = 1 ]; then gh api -X DELETE "repos/$REPO/milestones/$num" >/dev/null; fi
done

# GraphQL variables use $o/$n for gh -f; shell must not expand them.
# shellcheck disable=SC2016
PJSON="$(gh api graphql -f query='query($o:String!,$n:Int!){user(login:$o){projectV2(number:$n){id views(first:50){nodes{id name}} field(name:"Status"){... on ProjectV2SingleSelectField{id options{id name}}}}}}' -f o="$OWNER" -F n="$PROJECT")" \
  || { echo "  ! cannot read project $PROJECT (gh auth refresh -s project); steps 4-5 skipped"; PJSON=""; }

echo "== 4. stale views"
if [ -n "$PJSON" ]; then
  for name in "Casos de Uso" "RNF"; do
    id="$(jq -r --arg n "$name" '.data.user.projectV2.views.nodes[]|select(.name==$n)|.id' <<<"$PJSON")"
    [ -n "$id" ] || continue
    say "delete view '$name'"
    if [ "$APPLY" = 1 ]; then
      # shellcheck disable=SC2016
      gh api graphql -f query='mutation($id:ID!){deleteProjectV2View(input:{viewId:$id}){clientMutationId}}' -f id="$id" >/dev/null \
        || echo "  ! delete '$name' in the UI (view ▾ → Delete view)"
    fi
  done
fi

echo "== 5. board Status of closed issues"
if [ -n "$PJSON" ]; then
  PID="$(jq -r '.data.user.projectV2.id' <<<"$PJSON")"
  FID="$(jq -r '.data.user.projectV2.field.id' <<<"$PJSON")"
  DONE="$(jq -r '.data.user.projectV2.field.options[]|select(.name=="Done")|.id' <<<"$PJSON")"
  gh project item-list "$PROJECT" --owner "$OWNER" --format json -L 3000 |
    jq -r '.items[]|select(.content.type=="Issue" and (.status//"")!="Done")|"\(.id)\t\(.content.number)"' |
  while IFS=$'\t' read -r item num; do
    state="$(gh api "repos/$REPO/issues/$num" -q .state)"
    [ "$state" = closed ] || continue
    say "set #$num Status -> Done"
    if [ "$APPLY" = 1 ]; then
      gh project item-edit --id "$item" --project-id "$PID" --field-id "$FID" --single-select-option-id "$DONE" >/dev/null
    fi
  done
fi

echo "== 6. verify"
if [ "$APPLY" = 1 ]; then
  python3 workspace/sdlc/apply_milestone_policy.py --check || true
  python3 workspace/sdlc/check-board-status-label-coherence.py --scope wip || true
  echo
  echo "Snapshots: docs/github/milestone-snapshot.json and github-page/lib/milestone-snapshot.json were rewritten."
  echo "Do not commit on main; tell the agent to open the snapshot PR (or: git stash and hand over)."
else
  echo "  preview only. Re-run with --apply."
fi
