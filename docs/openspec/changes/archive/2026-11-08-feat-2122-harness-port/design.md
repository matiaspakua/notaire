> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2121 factory foundation. V1 assessment HarnessPort.

## Goals / Non-Goals

**Goals:** One harness port; two adapters; no sandbox claim; files stay.
**Non-Goals:** Isolator, agent-never-commits, Spec Kit, OTel.

## Decisions

Stdlib field extractor instead of a YAML dependency; profile declares paths so the kit stays adopter-free.

## Riesgos / Trade-offs

Field extractor may miss exotic YAML; mitigated by focused fields and adopter tests on real files.

## Testing Strategy

framework/ai-sdlc/tests/test_harness.py and workspace/tests/test_harness_adoption.py; bash framework/ai-sdlc/verify.sh

## Regression Strategy

config/plugin/inventory from #2121 still pass

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
