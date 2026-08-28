#!/usr/bin/env bash
#
# validate-speckit-plan.sh — enforce the Engineering Constitution on SpecKit features.
#
# WHY
# ---
# CONSTITUTION.md defines the mandatory SDLC and its five Quality Gates.
# scripts/validate-sdlc-plan.sh already mechanically enforces this for OpenSpec
# changes. SpecKit (speckit/) is an alternative spec-driven framework adopted
# for evaluation, adapted to the same Constitution the same way: project-owned
# template deltas (advisory) plus this script (the actual gate, agent-agnostic).
# It is plain bash so it works for any agent and any human — Claude Code,
# OpenCode, Copilot, Codex, Cursor, CI — with no tool-specific hooks.
#
# WHAT IT CHECKS
#   spec.md          Notaire Traceability header (Issue + Use Case) + the
#                     Issue exists and is open on GitHub (live check via `gh`,
#                     when available)
#   spec.md           at least one Given/When/Then Acceptance Scenario
#   plan.md           Testing / Regression / Playwright / Deployment / Rollback
#                     Strategy sections
#   tasks.md          the twelve mandatory SDLC groups + Definition of Done
#   traceability.md   the Issue → ... → Release chain is present and not faked
#
# Completed features (Issue closed, PR merged) move to speckit/specs/archive/,
# mirroring openspec/changes/archive/ — that directory is skipped below, so a
# feature's Issue is only required to stay OPEN while it is still active.
#
# Only feature directories under speckit/specs/ are governed by this script;
# it never touches openspec/.
#
# USAGE
#   bash scripts/validate-speckit-plan.sh                 # every feature
#   bash scripts/validate-speckit-plan.sh <feature-dir>    # one feature (e.g. 001-cu03-checklist)
#   bash scripts/validate-speckit-plan.sh --list           # what is checked, and why
#
# Exit code is non-zero if any feature has an incomplete plan.

set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SPECS_DIR="$REPO_ROOT/speckit/specs"

if [ -t 1 ]; then
  RED=$'\033[0;31m'; GREEN=$'\033[0;32m'; YELLOW=$'\033[0;33m'
  BLUE=$'\033[0;34m'; NC=$'\033[0m'
else
  RED=''; GREEN=''; YELLOW=''; BLUE=''; NC=''
fi

ERRORS=0
CHECKED=0

section() { printf '\n%s=== %s ===%s\n' "$BLUE" "$1" "$NC"; }
ok()      { printf '  %s✓%s %s\n' "$GREEN" "$NC" "$1"; }
bad()     { printf '  %s✗%s %s\n' "$RED" "$NC" "$1"; ERRORS=$((ERRORS + 1)); }
note()    { printf '  %s-%s %s\n' "$YELLOW" "$NC" "$1"; }

# need_file <path> <label>
need_file() {
  if [ -f "$1" ]; then
    return 0
  fi
  bad "$2 is missing ($(basename "$1"))"
  return 1
}

# need_section <file> <regex> <human label>
need_section() {
  if grep -qE "$2" "$1" 2>/dev/null; then
    return 0
  fi
  bad "$(basename "$1"): missing section \"$3\""
  return 1
}

# gh_available — true if the gh CLI is installed and authenticated
gh_available() {
  command -v gh >/dev/null 2>&1 && gh auth status >/dev/null 2>&1
}

# check_issue_live <issue-number>
# Resolves the Issue on GitHub and requires it to exist and be open.
# Caller must confirm gh_available first; this only queries.
check_issue_live() {
  local number="$1"
  local state
  state="$(gh issue view "$number" --json state --jq '.state' 2>/dev/null)"
  if [ -z "$state" ]; then
    bad "spec: Issue #$number was not found on GitHub (Explore → Issue → Specify, Gate 1)"
    return 1
  fi
  if [ "$state" != "OPEN" ]; then
    bad "spec: Issue #$number exists but is $state, not OPEN"
    return 1
  fi
  ok "spec: Issue #$number verified live on GitHub (OPEN)"
  return 0
}

usage_list() {
  cat <<'EOF'
Local check                          Constitution reference
-----------------------------------  ------------------------------------------
spec: Issue + Use Case                §4 Mandatory Conventions / Gate 1
spec: Issue exists + is open          Explore → Issue → Specify / Gate 1 (needs gh)
spec: Given/When/Then scenarios       Gate 1 / §7 Testing Rules
plan: Testing Strategy                P1 TDD / Gate 2
plan: Regression Strategy             §7 Testing Rules / Gate 3
plan: Playwright Strategy             §7 (E2E mandatory for UI) / Gate 3
plan: Deployment + Rollback Strategy  §11 Release Rules / Gate 5
tasks: 12 mandatory SDLC groups       §5 Official SDLC Workflow
tasks: Definition of Done             §3 Definition of Done
traceability: full chain, not faked   P4 Traceability
EOF
}

validate_feature() {
  local dir="$1"
  local name
  name="$(basename "$dir")"

  section "$name"
  CHECKED=$((CHECKED + 1))

  # ------------------------------------------------------------------- spec
  local spec="$dir/spec.md"
  if need_file "$spec" "spec"; then
    need_section "$spec" '^## Notaire Traceability' 'Notaire Traceability'

    local issue_number
    issue_number="$(grep -oE '^\|[[:space:]]*\*\*GitHub Issue\*\*[[:space:]]*\|[[:space:]]*#[0-9]+' "$spec" \
      | grep -oE '#[0-9]+' | tr -d '#')"
    if [ -n "$issue_number" ]; then
      ok "spec: GitHub Issue referenced (#$issue_number)"
      if gh_available; then
        check_issue_live "$issue_number"
      else
        note "spec: gh CLI not available/authenticated — Issue #$issue_number not live-verified"
      fi
    else
      bad "spec: no GitHub Issue number (Gate 1 — every feature needs an Issue)"
    fi

    if grep -qE '^\|[[:space:]]*\*\*Use Case\*\*[[:space:]]*\|[[:space:]]*[^|[:space:]<[]' "$spec"; then
      ok "spec: Use Case referenced"
    else
      bad "spec: no Use Case reference (CU-XX / RF-XX / RNF-XX)"
    fi

    local scenarios
    scenarios=$(grep -cE '\*\*Given\*\*.*\*\*When\*\*.*\*\*Then\*\*' "$spec" 2>/dev/null || echo 0)
    if [ "${scenarios:-0}" -eq 0 ]; then
      bad "spec: no Given/When/Then Acceptance Scenario found — these ARE the Acceptance Criteria (Gate 1)"
    else
      ok "spec: $scenarios Given/When/Then scenario(s) as Acceptance Criteria"
    fi
  fi

  # ------------------------------------------------------------------- plan
  local plan="$dir/plan.md"
  if need_file "$plan" "plan"; then
    need_section "$plan" '^## Testing Strategy'    'Testing Strategy'
    need_section "$plan" '^## Regression Strategy' 'Regression Strategy'
    need_section "$plan" '^## Playwright Strategy' 'Playwright Strategy'
    need_section "$plan" '^## Deployment Strategy' 'Deployment Strategy'
    need_section "$plan" '^## Rollback Strategy'   'Rollback Strategy'
  fi

  # ------------------------------------------------------------------ tasks
  local tasks="$dir/tasks.md"
  if need_file "$tasks" "tasks"; then
    local -a group_labels=(
      "Gate 1 — Prerequisites"
      "Crear branch"
      "Tests for User Story phases (TDD)"
      "Implementation for User Story phases"
      "Actualizar tests existentes"
      "Ejecutar regresión"
      "Ejecutar Playwright"
      "Gate 3 — Documentación permanente"
      "Commits atómicos"
      "Pull Request y validación CI"
      "Deploy"
      "Gate 5 — Smoke test y cierre"
    )
    local -a group_patterns=(
      '^### Gate 1 — Prerequisites'
      '^### Crear branch'
      '^\*\*Tests for User Story|^### Tests for'
      '^\*\*Implementation for User Story|^### Implementation for'
      '^### Actualizar tests existentes'
      '^### Ejecutar regresión'
      '^### Ejecutar Playwright'
      '^### Gate 3 — Actualizar documentación permanente'
      '^### Commits atómicos'
      '^### Pull Request y validación CI'
      '^### Deploy'
      '^### Gate 5 — Smoke test y cierre'
    )
    local missing_groups=0
    local i
    for i in "${!group_patterns[@]}"; do
      if ! grep -qE "${group_patterns[$i]}" "$tasks"; then
        bad "tasks: missing mandatory group — ${group_labels[$i]}"
        missing_groups=$((missing_groups + 1))
      fi
    done
    if [ "$missing_groups" -eq 0 ]; then
      ok "tasks: all 12 mandatory SDLC groups present"
    fi

    need_section "$tasks" '^## Definition of Done' 'Definition of Done'

    local boxes
    boxes=$(grep -cE '^- \[[ xX]\] ' "$tasks" 2>/dev/null || echo 0)
    if [ "$boxes" -lt 12 ]; then
      bad "tasks: only $boxes checkbox task(s) — the plan looks like unfilled boilerplate"
    else
      ok "tasks: $boxes trackable checkbox task(s)"
    fi
  fi

  # ------------------------------------------------------------ traceability
  local trace="$dir/traceability.md"
  if need_file "$trace" "traceability"; then
    need_section "$trace" '^## Chain'                            'Chain'
    need_section "$trace" '^## Requirement coverage'             'Requirement coverage'
    need_section "$trace" '^## Permanent documentation updated'  'Permanent documentation updated'
    need_section "$trace" '^## Gate log'                         'Gate log'
    need_section "$trace" '^## Exceptions'                       'Exceptions'
  fi
}

# ------------------------------------------------------------------- main
if [ "${1:-}" = "--list" ]; then
  usage_list
  exit 0
fi

printf '%sSpecKit plan validation%s — CONSTITUTION.md enforced on SpecKit features\n' "$BLUE" "$NC"

if [ ! -d "$SPECS_DIR" ]; then
  printf '%s\n' "No speckit/specs directory — nothing to validate."
  exit 0
fi

if [ -n "${1:-}" ]; then
  target="$SPECS_DIR/$1"
  if [ ! -d "$target" ]; then
    printf '%sFeature not found:%s %s\n' "$RED" "$NC" "$1"
    exit 1
  fi
  validate_feature "$target"
else
  shopt -s nullglob
  for d in "$SPECS_DIR"/*/; do
    [ "$(basename "$d")" = "archive" ] && continue
    validate_feature "${d%/}"
  done
  shopt -u nullglob
fi

echo
if [ "$CHECKED" -eq 0 ]; then
  printf '%sNo SpecKit features found under speckit/specs/ to validate.%s\n' "$YELLOW" "$NC"
  exit 0
fi

if [ "$ERRORS" -gt 0 ]; then
  printf '%s✗ %d problem(s) across %d feature(s).%s\n' "$RED" "$ERRORS" "$CHECKED" "$NC"
  printf 'An incomplete plan does not pass Gate 1. See CONSTITUTION.md §5 and §6.\n'
  exit 1
fi

printf '%s✓ %d feature(s) conform to the Engineering Constitution.%s\n' "$GREEN" "$CHECKED" "$NC"
