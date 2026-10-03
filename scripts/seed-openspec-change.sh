#!/usr/bin/env bash
#
# seed-openspec-change.sh — copy notaire-sdlc templates into a change folder.
#
# WHY
# ---
# `openspec new change` only creates `.openspec.yaml`. Agents that write
# proposal/design/tasks/traceability from scratch often omit mandatory headings
# (see #1049 / #1062). Seeding the schema templates gives every agent the same
# skeleton to fill; `scripts/validate-sdlc-plan.sh` then rejects leftover
# `<!-- ... -->` section bodies.
#
# USAGE
#   bash scripts/seed-openspec-change.sh <change-name> \
#     [--issue N] [--use-case "CU76 — …"] [--branch "type/N_desc"] [--create]
#
# --create  run `openspec new change <name>` when the folder is missing
#
# Never overwrites an existing non-empty artifact file.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TEMPLATES="$REPO_ROOT/openspec/schemas/notaire-sdlc/templates"
CHANGES_DIR="$REPO_ROOT/openspec/changes"

CHANGE_NAME=""
ISSUE=""
USE_CASE=""
BRANCH=""
CREATE=0

usage() {
  cat <<'EOF'
Usage: bash scripts/seed-openspec-change.sh <change-name> [options]

Options:
  --issue N              GitHub Issue number (digits only)
  --use-case TEXT        Use Case cell (e.g. "CU76 — Quality Assurance…")
  --branch NAME          Branch name for header / traceability tables
  --create               Run `openspec new change` if the folder is missing
  -h, --help             Show this help
EOF
}

while [ $# -gt 0 ]; do
  case "$1" in
    --issue) ISSUE="${2:-}"; shift 2 ;;
    --use-case) USE_CASE="${2:-}"; shift 2 ;;
    --branch) BRANCH="${2:-}"; shift 2 ;;
    --create) CREATE=1; shift ;;
    -h|--help) usage; exit 0 ;;
    -*)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 2
      ;;
    *)
      if [ -z "$CHANGE_NAME" ]; then
        CHANGE_NAME="$1"
        shift
      else
        echo "Unexpected argument: $1" >&2
        exit 2
      fi
      ;;
  esac
done

if [ -z "$CHANGE_NAME" ]; then
  usage >&2
  exit 2
fi

CHANGE_DIR="$CHANGES_DIR/$CHANGE_NAME"

if [ ! -d "$CHANGE_DIR" ]; then
  if [ "$CREATE" -eq 1 ]; then
    (cd "$REPO_ROOT" && openspec new change "$CHANGE_NAME" --schema notaire-sdlc) \
      || { echo "openspec new change failed" >&2; exit 1; }
  else
    echo "Change not found: $CHANGE_DIR (pass --create to scaffold it)" >&2
    exit 1
  fi
fi

if [ ! -d "$TEMPLATES" ]; then
  echo "Templates missing: $TEMPLATES" >&2
  exit 1
fi

seed_one() {
  local name="$1"
  local dest="$CHANGE_DIR/$name"
  local src="$TEMPLATES/$name"
  if [ -f "$dest" ] && [ -s "$dest" ]; then
    echo "keep  $name (already present)"
    return 0
  fi
  cp "$src" "$dest"
  echo "seed  $name"
}

seed_one proposal.md
seed_one design.md
seed_one tasks.md
seed_one traceability.md

# BSD and GNU sed disagree on `-i`, so edit through a temp file instead.
sed_edit() {
  local file="$1"
  shift
  sed -E "$@" "$file" > "$file.new" && mv "$file.new" "$file"
}

fill_known_values() {
  local file="$1"
  [ -f "$file" ] || return 0
  local tmp
  tmp="$(mktemp)"
  cp "$file" "$tmp"

  if [ -n "$ISSUE" ]; then
    # proposal header + traceability chain placeholders
    sed_edit "$tmp" \
      -e "s/\|[[:space:]]*GitHub Issue[[:space:]]*\|[[:space:]]*#?<!--[^|]*-->[[:space:]]*\|/| GitHub Issue | #${ISSUE} |/" \
      -e "s/\|[[:space:]]*Issue[[:space:]]*\|[[:space:]]*#?<!--[^|]*-->[[:space:]]*\|[^|]*\|/| Issue | #${ISSUE} | in-progress |/"
  fi

  if [ -n "$USE_CASE" ]; then
    # Escape sed replacement metacharacters in USE_CASE
    local uc_esc
    uc_esc="$(printf '%s' "$USE_CASE" | sed -e 's/[&|\\]/\\&/g')"
    sed_edit "$tmp" \
      -e "s/\|[[:space:]]*Use Case[[:space:]]*\|[[:space:]]*<!--[^|]*-->[[:space:]]*\|/| Use Case | ${uc_esc} |/" \
      -e "s/\|[[:space:]]*Use Case[[:space:]]*\|[[:space:]]*<!--[^|]*-->[[:space:]]*\|[^|]*\|/| Use Case | ${uc_esc} | exists |/"
  fi

  if [ -n "$BRANCH" ]; then
    local br_esc
    br_esc="$(printf '%s' "$BRANCH" | sed -e 's/[&|\\]/\\&/g')"
    sed_edit "$tmp" \
      -e "s#\|[[:space:]]*Branch[[:space:]]*\|[[:space:]]*\`<type>/[^\\\`]*\`[[:space:]]*\|#| Branch | \`${br_esc}\` |#" \
      -e "s#\|[[:space:]]*Branch[[:space:]]*\|[[:space:]]*\`<type>/[^\\\`]*\`[[:space:]]*\|[^|]*\|#| Branch | \`${br_esc}\` | created |#"
  fi

  # Always point the Specification path at this change name when still templated.
  sed_edit "$tmp" \
    -e "s#openspec/changes/<change-name>/#openspec/changes/${CHANGE_NAME}/#g" \
    -e "s#\|[[:space:]]*Specification[[:space:]]*\|[[:space:]]*\`openspec/changes/<change-name>/\`[[:space:]]*\|[^|]*\|#| Specification | \`openspec/changes/${CHANGE_NAME}/\` | seeded |#"

  mv "$tmp" "$file"
}

fill_known_values "$CHANGE_DIR/proposal.md"
fill_known_values "$CHANGE_DIR/traceability.md"

echo "Seeded OpenSpec change: $CHANGE_DIR"
echo "Next: fill every ## section (keep headings), then:"
echo "  openspec validate $CHANGE_NAME --strict"
echo "  bash scripts/validate-sdlc-plan.sh $CHANGE_NAME"
