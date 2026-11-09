> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

factory-stop-after-2172.md; epic #2134 closed; no Owner runtime_*

## Goals / Non-Goals

**Goals:** Honest Isolator boundary design + unevaluable stub; no fake sandbox.
**Non-Goals:** Implementing OS isolation, setting runtime_*, ForgePort, portal.

## Decisions

ADR-004 + IsolatorPort stub status(); doctor INFO line; do not flip committer required.

## Riesgos / Trade-offs

Readers confuse stub with real Isolator — mitigated by sandbox=false and ADR language.

## Testing Strategy

IsolatorPortTest + doctor output assertion

## Regression Strategy

framework/ai-sdlc doctor/init tests

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
