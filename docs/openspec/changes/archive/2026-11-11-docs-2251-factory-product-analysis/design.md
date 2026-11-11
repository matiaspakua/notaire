> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Source: issue #2251 body, re-verified against main d90e106. Corrections: F4 path list, F11 (#2088 closed), F12 (#2240 TBD fixed).

## Goals / Non-Goals

**Goals:** Permanent contract; facts verified; doc gates green.
**Non-Goals:** Code, ADR for open decisions, product UI changes.

## Decisions

Two documents split as the issue directs (sections 1,2,9,10 vs 3-8,11). Owner decisions copied as decision packets, unanswered.

## Riesgos / Trade-offs

Docs drift from the issue until the Owner answers; mitigated by the contract rule that deviations are commented on #2251.

## Testing Strategy

workspace/tests/test_factory_roadmap.py link and decision-marker tests; markdownlint-cli2; check-doc-traceability; validate-sdlc-plan.

## Regression Strategy

Existing FactoryRoadmapTest needles unchanged and still passing.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
