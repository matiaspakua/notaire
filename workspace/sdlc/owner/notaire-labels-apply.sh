#!/usr/bin/env bash
# notaire-labels-apply.sh — label taxonomy rename (#1860), run by the Owner AFTER PR #1863 merges.
#
#   bash notaire-labels-apply.sh            # preview (default): prints every action, changes nothing
#   bash notaire-labels-apply.sh --apply    # performs it
#
# Idempotent: every step checks live state first, so a re-run continues where a
# failed run stopped and a finished run reports "nothing to do".
#   1. rename legacy -> new (GitHub rename keeps the label on every issue and PR)
#   2. merge: when the new name already exists, move each issue/PR's label, then delete the old one
#   3. create labels with no legacy name (release:breaking)
#   4. delete phase:* and unused labels
#   5. delete project fields Priority / Area / Kind on user project 1 (needs the `project` scope)
# The table mirrors workspace/sdlc/label_taxonomy.py (RENAMES / CREATE / DELETE).
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

# old|new   (order matters: chore before TAREA so TAREA merges into the renamed chore)
MAP='BACKEND|area:backend
FRONTEND|area:frontend
DB|area:db
DEVOPS|area:devops
TEST|area:test
DOC|area:docs
enhancement|type:feature
bug|type:bug
REFACTOR|type:refactor
chore|type:chore
TAREA|type:chore
ci|type:ci
roadmap|type:epic
in-progress|status:in-progress
ready-for-dev|status:ready
in-review|status:in-review
blocked|status:blocked
needs-info|status:needs-info
security|concern:security
a11y|concern:a11y
i18n|concern:i18n
ux|concern:ux
risk|concern:risk
tech-debt|concern:tech-debt
requerimiento-funcional|trace:rf
requerimiento-no-funcional|trace:rnf
CASO-DE-USO|trace:cu
audit-2026-09|source:audit-2026-09
sdlc-exception|process:sdlc-exception'

CREATE='release:breaking|b60205|Breaking change: semver major, CHANGELOG Breaking section'

DELETE='phase:1-analysis
phase:2-foundation
phase:3-backend-api
phase:4-frontend
phase:5-observability
phase:6-deprecation
Cursor
duplicate
invalid
question
good first issue
help wanted'

FIELDS='Priority
Area
Kind'

enc() { python3 -c 'import sys,urllib.parse;print(urllib.parse.quote(sys.argv[1],safe=""))' "$1"; }
run() { if [ "$APPLY" = 1 ]; then "$@"; fi; }
say() { if [ "$APPLY" = 1 ]; then echo "  do: $*"; else echo "  would: $*"; fi; }

command -v gh >/dev/null || { echo "gh is required" >&2; exit 1; }
gh auth status >/dev/null 2>&1 || { echo "gh is not logged in" >&2; exit 1; }

labels() { gh api --paginate "repos/$REPO/labels?per_page=100" -q '.[].name'; }
has() { grep -qxF -- "$1" <<<"$LIVE"; }
items_with() { gh api --paginate "repos/$REPO/issues?state=all&per_page=100&labels=$(enc "$1")" -q '.[].number'; }

LIVE="$(labels)"
changes=0
echo "== 1-2. rename / merge ($([ $APPLY = 1 ] && echo APPLY || echo PREVIEW))"
while IFS='|' read -r old new; do
  [ -n "$old" ] || continue
  if ! has "$old"; then continue; fi
  changes=$((changes+1))
  if ! has "$new"; then
    say "rename '$old' -> '$new'"
    run gh api -X PATCH "repos/$REPO/labels/$(enc "$old")" -f new_name="$new" >/dev/null
  else
    nums="$(items_with "$old" | tr '\n' ' ')"
    say "merge '$old' into existing '$new' on: ${nums:-<none>}"
    for n in $nums; do
      run gh api -X POST "repos/$REPO/issues/$n/labels" -f "labels[]=$new" >/dev/null
      run gh api -X DELETE "repos/$REPO/issues/$n/labels/$(enc "$old")" >/dev/null
    done
    if [ "$APPLY" = 1 ] && [ -n "$(items_with "$old")" ]; then
      echo "  ! '$old' still on issues; not deleted (re-run)"; continue
    fi
    say "delete label '$old'"
    run gh api -X DELETE "repos/$REPO/labels/$(enc "$old")" >/dev/null
  fi
  [ "$APPLY" = 1 ] && LIVE="$(labels)" || LIVE="$(printf '%s\n%s\n' "$LIVE" "$new" | grep -vxF -- "$old")"
done <<<"$MAP"

echo "== 3. create"
while IFS='|' read -r name color desc; do
  has "$name" && continue
  changes=$((changes+1))
  say "create '$name'"
  run gh api -X POST "repos/$REPO/labels" -f name="$name" -f color="$color" -f description="$desc" >/dev/null
done <<<"$CREATE"
if has "area:frontend" || [ "$APPLY" = 0 ]; then
  say "set area:frontend description"
  run gh api -X PATCH "repos/$REPO/labels/$(enc area:frontend)" -f description="Next.js web client: pages, forms, and dashboard UI" >/dev/null
fi

echo "== 4. delete phase:* and unused"
while IFS= read -r name; do
  if [ -z "$name" ] || ! has "$name"; then
    continue
  fi
  changes=$((changes+1))
  count="$(items_with "$name" | wc -l | tr -d ' ')"
  say "delete label '$name' (removes it from $count issue(s)/PR(s))"
  run gh api -X DELETE "repos/$REPO/labels/$(enc "$name")" >/dev/null
done <<<"$DELETE"

echo "== 5. project fields"
# GraphQL variables use $o/$n for gh -f; shell must not expand them.
# shellcheck disable=SC2016
FIELD_JSON="$(gh api graphql -f query='query($o:String!,$n:Int!){user(login:$o){projectV2(number:$n){id fields(first:50){nodes{... on ProjectV2FieldCommon{id name}}}}}}' -f o="$OWNER" -F n="$PROJECT" 2>/dev/null || true)"
if [ -z "$FIELD_JSON" ]; then
  echo "  ! cannot read project $PROJECT (token lacks the project scope: gh auth refresh -s project)."
  echo "    MANUAL: Project settings -> Custom fields -> delete Priority, Area, Kind."
else
  while IFS= read -r field; do
    id="$(jq -r --arg f "$field" '.data.user.projectV2.fields.nodes[]|select(.name==$f)|.id' <<<"$FIELD_JSON")"
    [ -n "$id" ] || continue
    changes=$((changes+1))
    say "delete project field '$field' ($id) — values on every item are lost"
    if [ "$APPLY" = 1 ]; then
      # shellcheck disable=SC2016
      gh api graphql -f query='mutation($id:ID!){deleteProjectV2Field(input:{fieldId:$id}){clientMutationId}}' -f id="$id" >/dev/null \
        || echo "  ! could not delete '$field'; MANUAL: Project settings -> Custom fields -> delete '$field'"
    fi
  done <<<"$FIELDS"
fi

echo
if [ "$changes" = 0 ]; then echo "nothing to do: taxonomy already applied"
elif [ "$APPLY" = 0 ]; then echo "preview: $changes change(s). Run with --apply to perform them."
else echo "applied. Re-run without flags to confirm 'nothing to do'."; fi
