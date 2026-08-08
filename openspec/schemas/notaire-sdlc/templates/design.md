> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

<!-- Current state and constraints that shape the approach. See proposal.md for
     motivation - don't restate it. -->

## Goals / Non-Goals

**Goals:**
<!-- What this design aims to achieve -->

**Non-Goals:**
<!-- What is explicitly out of scope -->

## Decisions

<!-- Key design decisions with rationale and alternatives considered.
     For each: why X over Y? -->

## Riesgos / Trade-offs

<!-- Known risks, limitations, things that could go wrong.
     Format: [Risk] → Mitigation. A risk you cannot mitigate must still be
     stated; an undeclared limitation is worse than a declared one. -->

## Testing Strategy

<!-- How the scenarios in the delta spec become tests. Every Acceptance Criterion
     (each `#### Scenario:`) must map to at least one test.
     TDD is mandatory (Constitution P1): tests are written and observed FAILING
     before implementation. -->

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| <!-- scenario name --> | unit / integration / E2E | <!-- --> |

- New unit tests (`src/test/java/.../unit/`):
- New integration tests (`src/test/java/.../integration/`):
- Coverage impact (JaCoCo ratchet floor; 80% target):

## Regression Strategy

<!-- What existing behavior could this break, and how it is proven not to.
     Name the existing test classes that must be updated and why. Never weaken an
     assertion to force green - if an existing test now fails legitimately, say why
     the old expectation was wrong. -->

- Existing tests affected:
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash integration-test/scripts/test.sh`
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`):

## Playwright Strategy

<!-- Mandatory for any UI change. If this change has no UI surface, write
     "n/a - no UI surface" and justify it in one line; do not delete the section. -->

- Specs to add/update under `frontend/tests/e2e/`:
- Golden path covered:
- Edge / error paths covered:
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

<!-- How this reaches an environment. Order of operations matters when a schema
     migration and code change ship together. -->

- Flyway migration required: <!-- yes (V{n}__...) / no -->
- Deployment order / coupling:
- Configuration or `.env` keys to add (add to `.env.example`, never commit secrets):
- Feature flag: <!-- yes/no -->
- Smoke test after deploy (Gate 5):

## Rollback Strategy

<!-- How to undo this if the smoke test or production behavior fails.
     A revert is only a valid rollback if it is actually safe - state whether it is. -->

- Revert safe: <!-- yes / no + why -->
- Database rollback: <!-- none needed / R{n} script / forward-fix only -->
- Data written under the new behavior after revert:
- Blast radius if rollback is delayed:

## Migration Plan

<!-- Ordered steps to get from the current state to the target state. Omit if the
     change needs no staged rollout beyond the deployment strategy above. -->

## Open Questions

<!-- Genuinely deferrable unknowns only. If a question would change the specs, the
     approach, or the task breakdown, resolve it with a human now - do not bake an
     unstated assumption into the plan. Omit the section if there are none. -->
