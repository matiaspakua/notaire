> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Before this change, `python3 workspace/sdlc/check-openspec-archive-hygiene.py --inventory-only` lists 32 changes, each with "linked issue #N is CLOSED but the change is still active". `bash workspace/sdlc/validate-sdlc-plan.sh` fails with "Issue #N exists but is CLOSED, not OPEN" for the same set, and CI runs it on every PR.

## Goals / Non-Goals

**Goals:**

- Archive the 32 so that both checks pass.
- Keep every archived requirement and scenario text intact.

**Non-Goals:**

- Slice changes of open umbrellas, validator changes, playbook changes (#1623 later slices).

## Decisions

1. **Fix the delta header, not the content.** Archive fails when the operation does not match the main spec. The rule applied: if every requirement in a section already exists, the header is `MODIFIED`. If none exists, it is `ADDED`. No section mixed new and existing requirements.
2. **One PR for all 32.** The diff is mostly renames into `changes/archive/`, plus the synced spec files. Splitting would leave PR Validation red on every PR for longer.

## Riesgos / Trade-offs

- [A wrong ADDED/MODIFIED choice] → `openspec archive` validates the merge and aborts otherwise. All 32 archived. `openspec validate --specs --strict` passes 168/168.

## Testing Strategy

The checks act as the tests. Both were red before and green after:
`check-openspec-archive-hygiene.py --inventory-only`, `validate-sdlc-plan.sh`, `openspec validate --specs --strict`.

## Regression Strategy

`preflight.sh` (process self-tests include OpenSpec tooling tests).

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Docs only, nothing deploys.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
