> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1282, Use Case CU76; slice 2a of #577. 59 mapping methods sit on the entities; about 14 controllers still use some of them as their live contract.

## Goals / Non-Goals

**Goals:** delete the methods the compiler proves dead; keep the rest from growing.
**Non-Goals:** converting the 14 controllers that still use the mapping, or the entity-to-entity chains behind them (later slices).

## Decisions

1. Dead means "removing it leaves `src/main` compiling", checked method by method until nothing more can be removed. Rejected: grep for callers (misses overloads and receivers of the same name on other types).
2. The `@JsonIgnore` above a deleted getter is deleted with it; a leftover annotation would silently attach to the next member.
3. The ratchet reads the baseline from a resource and uses reflection over `com.licensis.notaire.business`, the same approach as `ControllerSignatureArchitectureTest`.
4. Shared coverage tests (`EntitiesBasicTest`) lose only the statements and assertions that exercised a deleted method.

## Riesgos / Trade-offs

- Reflection-only callers are not covered by the compiler; the full suite and the unchanged OpenAPI export are the evidence.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| A new mapping method is rejected | unit | `EntityDtoMappingRatchetTest` |
| A removed method must leave the baseline | unit | `EntityDtoMappingRatchetTest` |
| Production code does not need the deleted methods | integration | `mvn verify`, `scripts/export-openapi.sh --maven` diff |

- Coverage impact: the deleted code and its tests leave together; the ratchet floor must hold
- Updated tests: entity unit tests lose the tests of deleted methods

## Regression Strategy

- Full suite command: `mvn verify -pl backend-api; bash scripts/run_pipeline.sh`
- Bruno and Playwright unchanged

## Playwright Strategy

No UI change; the suite runs as regression evidence.

## Deployment Strategy

- Flyway migration required: no; configuration keys: none
- Smoke test after deploy (Gate 5): CD green

## Rollback Strategy

- Revert the PR.
