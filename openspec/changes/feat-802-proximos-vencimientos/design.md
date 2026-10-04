> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #802, Use Case CU42 – Informar próximos vencimientos (#195). #837 made due dates inheritable so the report finally has data; #802 asks for the missing endpoint and screen.

## Goals / Non-Goals

**Goals:** Staff can see which documents expire soon, with the data CU42 lists.
**Non-Goals:** Notifications or e-mail alerts; changing how due dates are computed.

## Decisions

1. Released documents are excluded: a released document needs no more follow-up. Rejected: listing everything and flagging released rows.
2. Overdue documents are excluded because CU42 is about upcoming expirations. Rejected: a combined overdue and upcoming list; it can be a separate report if requested.
3. The service receives today as a parameter so the rule is testable without a clock bean. Rejected: injecting a Clock bean for one use.

## Riesgos / Trade-offs

- The due date is a date without time; comparisons use whole days.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| A document due inside the window | unit | `UpcomingExpirationServiceTest` |
| Several documents inside the window | unit | `UpcomingExpirationServiceTest` |
| Outside or irrelevant documents | integration | `UpcomingExpirationControllerIntegrationTest` |
| Invalid window | integration | `UpcomingExpirationControllerIntegrationTest` |
| Default window | unit | `UpcomingExpirationServiceTest` |
| The screen lists the documents | e2e | `testing/e2e/tests/TS-0097-proximos-vencimientos-feature.spec.ts` |

- New unit tests (`src/test/java/.../unit/`): `UpcomingExpirationServiceTest`, `useProximosVencimientos` hook test
- New integration tests: `UpcomingExpirationControllerIntegrationTest` (H2) covering the window query
- Coverage impact: positive: new service and query paths are covered; the floor is unchanged

## Regression Strategy

- Existing tests affected: none; new classes
- Full suite command: `mvn verify -pl backend-api; npx vitest run; Playwright TS-0097`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit; the new screen lists a document due soon

## Rollback Strategy

- Revert the PR.
