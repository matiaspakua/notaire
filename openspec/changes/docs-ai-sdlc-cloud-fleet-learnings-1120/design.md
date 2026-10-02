# Design — Document Cursor Cloud AI SDLC fleet process learnings

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Fleet docs (#1111), cloud env scripts (#1112), refactoring-rules docs (#1113), and
OpenSpec seed (#1116) are on `main`. Operational failures during those merges are
not yet captured in permanent cloud docs.

## Goals / Non-Goals

**Goals:**
- Capture five concrete process learnings where agents will read them (checklist,
  fleet architecture, README index).
- Keep Gate 1 complete with `skip_specs: true` and CU76 / #1120 traceability.

**Non-Goals:**
- No product code, workflow YAML, or `local-ai/` edits.
- No change to `pr-validation.yml` (already fixed on `main` in #1111).
- No environment.json edit in this PR (document the Saved-card requirement only).

## Decisions

- **`skip_specs: true`**: documentation of process; no product requirement deltas.
- **Fold into existing files** rather than a new LEARNINGS.md: agents already open
  ENVIRONMENT-CHECKLIST and FLEET-ARCHITECTURE; avoid doc sprawl.
- **Prefer seed script**: document `scripts/seed-openspec-change.sh` as the Gate 1
  starting point (#1108 / #1116).
- **Closes keyword**: state explicitly that `Issue: #N` alone does not close GitHub
  issues on merge — commits/PR body need `Closes #<issue>`.

## Riesgos / Trade-offs

- [Agents miss the new section] → Place learnings in both checklist (ops) and
  architecture (foreman “must never”) so either entry point works.
- [Docs drift from fixed workflow] → Cite that `pr-validation.yml` on `main` already
  stopped head commits; agents must not reintroduce the pattern manually.

## Testing Strategy

No application tests. Verification:

- Grep the five learnings in `docs/300-development/304-ai-sdlc-cloud/`.
- `openspec validate docs-ai-sdlc-cloud-fleet-learnings-1120 --strict`
- `bash scripts/validate-sdlc-plan.sh docs-ai-sdlc-cloud-fleet-learnings-1120`
- Confirm no `local-ai/` references introduced as runtime dependencies.

## Regression Strategy

No application code. Confirm markdownlint/docs CI still passes; no Java/TS surface.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Nothing deployed. Merge makes docs available to Cloud agents on next checkout.

## Rollback Strategy

Plain `git revert` of the merge commit.

## Open Questions

None. Saving the Cursor Environment card remains a human dashboard action; this
change only documents the requirement.
