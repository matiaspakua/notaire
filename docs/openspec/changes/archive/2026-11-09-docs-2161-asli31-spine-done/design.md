> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

ASLI-31 after #2159 / #2160 merge.

## Goals / Non-Goals

**Goals:** Docs reflect completed epic spine; close #2134.
**Non-Goals:** New kit features; portal.

## Decisions

Keep issue numbers in the file so `test_factory_roadmap` needles stay valid;
restructure §7 to Landed + Done spine + Deferred.

## Riesgos / Trade-offs

None — documentation mirror only.

## Testing Strategy

`workspace.tests.test_factory_roadmap` — assert spine-done wording + retain issue needles.

## Regression Strategy

Existing roadmap existence / foundation link tests unchanged.

## Playwright Strategy

n/a — docs only

## Deployment Strategy

Docs only.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
