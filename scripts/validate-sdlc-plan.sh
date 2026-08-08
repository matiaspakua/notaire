#!/usr/bin/env bash
#
# validate-sdlc-plan.sh — enforce the Engineering Constitution on OpenSpec changes.
#
# WHY
# ---
# CONSTITUTION.md defines the mandatory SDLC and its five Quality Gates. The
# OpenSpec schema `openspec/schemas/notaire-sdlc` puts those requirements in front
# of every agent (templates + instructions + config context), but instructions are
# advisory: an agent can still emit an incomplete plan. This script is the
# mechanical gate. It is plain bash so it works for any agent and any human -
# Claude Code, OpenCode, Copilot, Codex, Cursor, CI - with no tool-specific hooks.
#
# WHAT IT CHECKS
#   proposal.md      mandatory sections + a real Issue and Use Case
#   traceability.md  the Issue → ... → Release chain is present and not pre-filled
#   specs/**.md      at least one delta (unless skip_specs), scenarios well-formed
#   design.md        testing / regression / Playwright / deployment / rollback
#   tasks.md         the twelve mandatory SDLC groups + Definition of Done
#
# Changes created with a different schema are skipped: this policy applies to the
# notaire-sdlc workflow only.
#
# USAGE
#   bash scripts/validate-sdlc-plan.sh                 # every active change
#   bash scripts/validate-sdlc-plan.sh <change-name>   # one change
#   bash scripts/validate-sdlc-plan.sh --list          # what is checked, and why
#
# Exit code is non-zero if any active change has an incomplete plan.

set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CHANGES_DIR="$REPO_ROOT/openspec/changes"
SCHEMA_NAME="notaire-sdlc"

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

usage_list() {
  cat <<'EOF'
Local check                          Constitution reference
-----------------------------------  ------------------------------------------
proposal: Issue + Use Case            §4 Mandatory Conventions / Gate 1
proposal: Objetivo, What Changes      §5 step 3 Specification
proposal: Reglas de negocio           §5 step 3 / §8 (cite, never duplicate)
proposal: Impact Analysis + módulos   §5 step 4 Impact Analysis
proposal: Documentation Impact        §8 Documentation Rules / Gate 3
traceability: full chain, not faked   P4 Traceability
specs: scenarios = Acceptance Crit.   Gate 1 / §7 Testing Rules
design: Testing Strategy              P1 TDD / Gate 2
design: Regression Strategy           §7 Testing Rules / Gate 3
design: Playwright Strategy           §7 (E2E mandatory for UI) / Gate 3
design: Deployment + Rollback         §11 Release Rules / Gate 5
tasks: 12 mandatory SDLC groups       §5 Official SDLC Workflow
tasks: Definition of Done             §3 Definition of Done
EOF
}

validate_change() {
  local dir="$1"
  local name
  name="$(basename "$dir")"

  # Only govern changes created with this schema.
  local meta="$dir/.openspec.yaml"
  if [ -f "$meta" ] && ! grep -qE "^\s*schema:\s*${SCHEMA_NAME}\s*$" "$meta"; then
    note "$name — skipped (schema is not $SCHEMA_NAME)"
    return 0
  fi

  section "$name"
  CHECKED=$((CHECKED + 1))

  # ---------------------------------------------------------------- proposal
  local proposal="$dir/proposal.md"
  if need_file "$proposal" "proposal"; then
    need_section "$proposal" '^## Objetivo'            'Objetivo'
    need_section "$proposal" '^## What Changes'        'What Changes'
    need_section "$proposal" '^## Reglas de negocio'   'Reglas de negocio'
    need_section "$proposal" '^## Capabilities'        'Capabilities'
    need_section "$proposal" '^## Impact Analysis'     'Impact Analysis'
    need_section "$proposal" '^### Módulos afectados'  'Módulos afectados'
    need_section "$proposal" '^## Documentation Impact' 'Documentation Impact'

    # A real issue number, not the template placeholder.
    if grep -qE '^\|[[:space:]]*GitHub Issue[[:space:]]*\|[[:space:]]*#[0-9]+' "$proposal"; then
      ok "proposal: GitHub Issue referenced"
    else
      bad "proposal: no GitHub Issue number (Gate 1 — every change needs an Issue)"
    fi

    if grep -qE '^\|[[:space:]]*Use Case[[:space:]]*\|[[:space:]]*[^|[:space:]<]' "$proposal"; then
      ok "proposal: Use Case referenced"
    else
      bad "proposal: no Use Case reference (CU-XX / RF-XX / RNF-XX)"
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

  # ------------------------------------------------------------------- specs
  local skip_specs=0
  if [ -f "$meta" ] && grep -qE '^\s*skip_specs:\s*true\s*$' "$meta"; then
    skip_specs=1
  fi

  if [ "$skip_specs" -eq 1 ]; then
    note "specs: skipped (skip_specs: true — no spec-level behavior change)"
  else
    local spec_count
    spec_count=$(find "$dir/specs" -name '*.md' -type f 2>/dev/null | wc -l | tr -d ' ')
    if [ "$spec_count" -eq 0 ]; then
      bad "specs: no delta spec found (set skip_specs: true only if behavior does not change)"
    else
      local scenarios
      scenarios=$(grep -rhcE '^#### Scenario:' "$dir/specs" 2>/dev/null | paste -sd+ - | bc 2>/dev/null || echo 0)
      if [ "${scenarios:-0}" -eq 0 ]; then
        bad "specs: no '#### Scenario:' found — scenarios ARE the Acceptance Criteria (Gate 1)"
      else
        ok "specs: $spec_count file(s), $scenarios scenario(s) as Acceptance Criteria"
      fi
      # 3-hashtag scenarios fail silently in OpenSpec's parser.
      if grep -rqE '^### Scenario:' "$dir/specs" 2>/dev/null; then
        bad "specs: found '### Scenario:' — scenarios need exactly four hashtags or they are ignored"
      fi
    fi
  fi

  # ------------------------------------------------------------------ design
  local design="$dir/design.md"
  if need_file "$design" "design (mandatory in this repository, unlike upstream OpenSpec)"; then
    need_section "$design" '^## Context'              'Context'
    need_section "$design" '^## Goals / Non-Goals'    'Goals / Non-Goals'
    need_section "$design" '^## Decisions'            'Decisions'
    need_section "$design" '^## Riesgos'              'Riesgos / Trade-offs'
    need_section "$design" '^## Testing Strategy'     'Testing Strategy'
    need_section "$design" '^## Regression Strategy'  'Regression Strategy'
    need_section "$design" '^## Playwright Strategy'  'Playwright Strategy'
    need_section "$design" '^## Deployment Strategy'  'Deployment Strategy'
    need_section "$design" '^## Rollback Strategy'    'Rollback Strategy'
  fi

  # ------------------------------------------------------------------- tasks
  local tasks="$dir/tasks.md"
  if need_file "$tasks" "tasks"; then
    local -a group_labels=(
      "Gate 1 — Prerequisites"
      "Crear branch"
      "Gate 2 — Escribir tests"
      "Implementación"
      "Actualizar tests existentes"
      "Ejecutar regresión"
      "Ejecutar Playwright"
      "Gate 3 — Documentación permanente"
      "Commits atómicos"
      "Pull Request y validación CI"
      "Deploy"
      "Gate 5 — Smoke test y cierre"
    )
    local missing_groups=0
    local i
    for i in $(seq 1 12); do
      if ! grep -qE "^## ${i}\." "$tasks"; then
        bad "tasks: missing mandatory group ${i} — ${group_labels[$((i - 1))]}"
        missing_groups=$((missing_groups + 1))
      fi
    done
    if [ "$missing_groups" -eq 0 ]; then
      ok "tasks: all 12 mandatory SDLC groups present"
    fi

    need_section "$tasks" '^## Definition of Done' 'Definition of Done'

    local boxes
    boxes=$(grep -cE '^- \[[ x]\] ' "$tasks" 2>/dev/null || echo 0)
    if [ "$boxes" -lt 12 ]; then
      bad "tasks: only $boxes checkbox task(s) — the plan looks like unfilled boilerplate"
    else
      ok "tasks: $boxes trackable checkbox task(s)"
    fi
  fi
}

# ------------------------------------------------------------------- main
if [ "${1:-}" = "--list" ]; then
  usage_list
  exit 0
fi

printf '%sSDLC plan validation%s — CONSTITUTION.md enforced on OpenSpec changes\n' "$BLUE" "$NC"

if [ ! -d "$CHANGES_DIR" ]; then
  printf '%s\n' "No openspec/changes directory — nothing to validate."
  exit 0
fi

if [ -n "${1:-}" ]; then
  target="$CHANGES_DIR/$1"
  if [ ! -d "$target" ]; then
    printf '%sChange not found:%s %s\n' "$RED" "$NC" "$1"
    exit 1
  fi
  validate_change "$target"
else
  shopt -s nullglob
  for d in "$CHANGES_DIR"/*/; do
    [ "$(basename "$d")" = "archive" ] && continue
    validate_change "${d%/}"
  done
  shopt -u nullglob
fi

echo
if [ "$CHECKED" -eq 0 ]; then
  printf '%sNo changes governed by %s to validate.%s\n' "$YELLOW" "$SCHEMA_NAME" "$NC"
  exit 0
fi

if [ "$ERRORS" -gt 0 ]; then
  printf '%s✗ %d problem(s) across %d change(s).%s\n' "$RED" "$ERRORS" "$CHECKED" "$NC"
  printf 'An incomplete plan does not pass Gate 1. See CONSTITUTION.md §5 and §6.\n'
  exit 1
fi

printf '%s✓ %d change(s) conform to the Engineering Constitution.%s\n' "$GREEN" "$CHECKED" "$NC"
