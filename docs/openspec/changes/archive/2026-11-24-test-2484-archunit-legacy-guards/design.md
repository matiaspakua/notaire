> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`HexagonArchitectureTest` compares each rule's violations with `hexagon-baseline.txt`; new lines fail, missing lines fail until shrink. The baseline was empty, so the rules looked done while the legacy packages were still widely used.

## Goals / Non-Goals

**Goals:** Make the stated rule executable without breaking the build.
**Non-Goals:** Migrating any context.

## Decisions

D1 A new rule R8 with its own baseline file instead of growing R2/R6: `aisdlc ratchet` refuses any line added to an existing baseline, and accepts a new baseline once (that is the sanctioned way to land a rule on legacy code).
D2 Generate the baseline with `-Darchitecture.baseline=init` once; later runs use shrink only.

## Riesgos / Trade-offs

A large baseline can read as accepted debt: #2485 and #2486 track its removal and the SAD links it.

## Testing Strategy

`LegacyPackageArchitectureTest` fails without a baseline (red) and passes with the generated one (green); `aisdlc ratchet` reports it as a new baseline.

## Regression Strategy

`UseCaseSliceCycleArchitectureTest`, `ControllerSignatureArchitectureTest` and the full `mvn test -pl backend-api` stay green.

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
