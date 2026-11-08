#!/usr/bin/env bash
# notaire-milestones-apply.sh — Owner-run, after PR #1847-nomenclature merges.
#
# Applies the version-first milestone nomenclature (MILESTONE-NOMENCLATURE.md)
# to GitHub: renames, template descriptions, due dates (none for Backlog and
# Catalog), new H0 / v0.1 / v0.2 / Catalog milestones, issue moves (catalog
# anchors → Catalog, jpa #571–#580 → v0.3, shipped issues → v0.1 / v0.2,
# v1.1 / Backlog bucket issues → H8), closing history and shipped milestones,
# then project dates, views and snapshots via apply_milestone_policy.py.
#
# Idempotent: every step compares the live state with the policy and only
# changes what differs; a second run prints no changes. Nothing is deleted.
#
# Usage:
#   bash notaire-milestones-apply.sh [path-to-notaire-checkout]          # dry run
#   bash notaire-milestones-apply.sh [path-to-notaire-checkout] --apply  # change GitHub
set -euo pipefail

REPO="matiaspakua/notaire"
ROOT="${1:-$PWD}"
MODE="${2:-}"
if [[ "${1:-}" == "--apply" ]]; then ROOT="$PWD"; MODE="--apply"; fi

die() { echo "error: $*" >&2; exit 1; }

command -v gh >/dev/null || die "gh CLI is required"
command -v python3 >/dev/null || die "python3 is required"
gh auth status >/dev/null 2>&1 || die "run 'gh auth login' as the repository owner (Issues + Projects write)"
[[ -f "$ROOT/workspace/sdlc/apply_milestone_policy.py" ]] || die "$ROOT is not a notaire checkout"

cd "$ROOT"
git fetch -q origin main
git -c advice.detachedHead=false checkout -q origin/main
grep -q 'RELEASE = "v0.3 · Hexagonal core & security (2026-11)"' workspace/sdlc/milestone_policy.py \
  || die "origin/main does not contain the nomenclature yet; merge the #1847 PR first"

export GH_TOKEN="${GH_TOKEN:-$(gh auth token)}"

echo "== Milestones before"
gh api "repos/$REPO/milestones?state=all&per_page=100" --jq '.[] | "\(.number)\t\(.state)\t\(.title)\t\(.due_on // "-")"' | sort -n

if [[ "$MODE" != "--apply" ]]; then
  echo "== Dry run (no changes). Re-run with --apply to change GitHub."
  python3 workspace/sdlc/apply_milestone_policy.py --dry-run
  exit 0
fi

echo "== Applying policy (renames, descriptions, due dates, new milestones, moves, close, project dates, views, snapshots)"
python3 workspace/sdlc/apply_milestone_policy.py --apply

# Backlog and Catalog must have no due date. GitHub only clears due_on when sent
# null explicitly; repeat it here so an older apply run cannot leave one behind.
for title in "Backlog · Unscheduled" "Catalog · Requirements & use cases"; do
  number=$(gh api "repos/$REPO/milestones?state=all&per_page=100" --jq ".[] | select(.title == \"$title\") | .number")
  [[ -n "$number" ]] || die "milestone '$title' is missing after apply"
  due=$(gh api "repos/$REPO/milestones/$number" --jq '.due_on // ""')
  if [[ -n "$due" ]]; then
    echo "clear due date on $title"
    gh api -X PATCH "repos/$REPO/milestones/$number" --input - <<<'{"due_on": null}' >/dev/null
  fi
done

echo "== Milestones after"
gh api "repos/$REPO/milestones?state=all&per_page=100" --jq '.[] | "\(.number)\t\(.state)\t\(.open_issues)/\(.closed_issues)\t\(.title)\t\(.due_on // "-")"' | sort -n

echo "== Drift check (must report 0 mismatches)"
python3 workspace/sdlc/apply_milestone_policy.py --check || true

cat <<'NOTE'

Next:
- The emptied former buckets ("History · v1.1 priority bucket", "History · Backlog
  priority bucket") stay as closed, empty milestones; delete them by hand if you wish.
- The refreshed snapshots (docs/github/milestone-snapshot.json and
  github-page/lib/milestone-snapshot.json) are written to this checkout: commit
  them through a small PR.
- In Project #1, keep the Roadmap view grouped by Milestone.
NOTE
