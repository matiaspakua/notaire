> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #851, Use Case CU44 – Reingresar testimonio (#197). #832 delivered the reenter endpoint at a simplified level; #851 records the gap against CU44 step 5.

## Goals / Non-Goals

**Goals:** The reenter movement keeps the data CU44 asks for; observed reentries explain themselves; the existing no-body call keeps working.
**Non-Goals:** Re-asking the exit and reentry dates (the exit date is already on the previous movement and the entry date is the reentry day); changing CU11/CU12.

## Decisions

1. Observed reentries require notes, because CU44 asks for observations when observed; a bare flag carries no information. Rejected: optional notes.
2. The body is optional so existing clients and tests keep working; defaults are 0, false, null. Rejected: a new endpoint.

## Riesgos / Trade-offs

- Existing rows get observed_by_registry false through the column default.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| A reentry with data | unit | `TestimonyMovementServiceTest` |
| A reentry without a body | integration | `TestimonyMovementControllerIntegrationTest` |
| Observed without notes | unit | `TestimonyMovementServiceTest` |
| The reentry dialog | e2e | `testing/e2e/tests/TS-0032-testimonio-inscripcion-feature.spec.ts` |

- New unit tests (`src/test/java/.../unit/`): `TestimonyMovementServiceTest`, `testimonio-movimiento-hooks.test.tsx`
- New integration tests: `TestimonyMovementControllerIntegrationTest` (H2) with the new body and validation cases
- Coverage impact: positive: the new service branches are covered; the floor is unchanged

## Regression Strategy

- Existing tests affected: TestimonyMovementControllerIntegrationTest, TestimonyMovementServiceTest, PersistableIdentityEntitiesIsNewTest if it builds the entity
- Full suite command: `mvn verify -pl backend-api; npx vitest run; Playwright TS-0032`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit; reentering a withdrawn testimony from the UI stores the data

## Rollback Strategy

- Revert the PR.
