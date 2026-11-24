> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`HexagonArchitectureTest` compares each rule's violations with `hexagon-baseline.txt`; new lines fail, missing lines fail until shrink. The baseline was empty, so the rules looked done while the legacy packages were still widely used.

## Goals / Non-Goals

**Goals:** Make the stated rule executable without breaking the build.
**Non-Goals:** Migrating any context.

## Decisions

D1 Extend R2 and R6 rather than add a rule: same meaning, same baseline format, `baselineIsWellFormed` keeps `R[1-6]`.
D2 Generate the new entries with `-Darchitecture.baseline=init` once: the other rules are empty, so init writes only the new R2/R6 lines. Later runs use shrink only.

## Riesgos / Trade-offs

A large baseline can read as accepted debt: #2485 and #2486 track its removal and the SAD links it.

## Testing Strategy

`HexagonArchitectureTest` fails with the extended rules and the old baseline (red), passes with the generated baseline (green); `shrink` mode keeps it.

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
