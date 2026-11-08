> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

See proposal.md — Objetivo. `createCompleteCase` is the next Management write on `controller-loose-returns.txt` whose success body is `DtoManagementSummary` and whose other arms are empty. Transition left the baseline in #2085.

## Goals / Non-Goals

**Goals:**

- Declare `ResponseEntity<DtoManagementSummary>` on `createCompleteCase`.
- Move the `CompleteCaseResult` switch into `CompleteCaseWebMapper#mapCreateResult`.
- Remove `ManagementController#createCompleteCase` from `controller-loose-returns.txt`.
- Keep 201 `DtoManagementSummary`, empty 400, empty 404, and empty 200.

**Non-Goals:**

- Closing #577.
- A `ManagementCompleteCaseExceptionHandler`.
- Typing `updateCompleteCase` or workflow trace.

## Decisions

Extract the existing switch into `CompleteCaseWebMapper` and declare `ResponseEntity<DtoManagementSummary>` on both the mapper and the controller. Empty `build()` calls infer `DtoManagementSummary` and still send no body.

Alternative considered: change only the controller return type and leave the switch inline. Rejected — the mapper keeps the controller thin and matches the transition pattern for HTTP mapping.

Alternative considered: throw domain exceptions and map empty bodies in advice. Rejected — the HTTP body would not change.

## Riesgos / Trade-offs

[Risk] None material. → One MockMvc class for the empty arms plus existing integration tests.

## Testing Strategy

- Shrink the baseline first so `ControllerSignatureArchitectureTest` fails while `createCompleteCase` is still `ResponseEntity<Object>`.
- `ManagementCreateCompleteCaseControllerTest`: 201 body, empty 400, empty 404, empty 200. The mapper is real.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| createCompleteCase leaves the loose baseline | unit | `ControllerSignatureArchitectureTest` |
| Create complete-case keeps the 201 body and empty error statuses | unit | `ManagementCreateCompleteCaseControllerTest` |

- New unit tests: `ManagementCreateCompleteCaseControllerTest`
- New integration tests: none (`ManagementControllerIntegrationTest` is PRESERVE)

## Regression Strategy

Existing complete-case assertions stay. No test casts the create body to `Object`.

- Existing tests affected: compile-time constructor arity on `ManagementController` tests (add mapper mock or real mapper)
- Full suite command: `mvn verify -pl backend-api`

## Playwright Strategy

n/a — no UI change in this tip.

## Deployment Strategy

Next release. No migration and no config change.

## Rollback Strategy

Revert the pull request.

## Migration Plan

None.

## Open Questions

None.
