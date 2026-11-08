> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

See proposal.md — Objetivo. `getWorkflowTrace` is the next Management write on `controller-loose-returns.txt` whose success body is `DtoManagementWorkflowTrace` and whose other arms are empty. Create-complete-case left the baseline in #2118.

## Goals / Non-Goals

**Goals:**

- Declare `ResponseEntity<DtoManagementWorkflowTrace>` on `getWorkflowTrace`.
- Move the `CompleteCaseResult` switch into `n/a#ManagementWorkflowTraceExceptionHandler`.
- Remove `ManagementController#getWorkflowTrace` from `controller-loose-returns.txt`.
- Keep 200 `DtoManagementWorkflowTrace`, empty 400, 400 with `{error}`.

**Non-Goals:**

- Closing #577.
- A `ManagementCompleteCaseExceptionHandler`.
- Typing `getWorkflowTrace` or audit/auxiliary handlers.

## Decisions

Extract the existing switch into `n/a#ManagementWorkflowTraceExceptionHandler` and declare `ResponseEntity<DtoManagementWorkflowTrace>` on both the mapper and the controller. Empty `build()` calls infer `DtoManagementWorkflowTrace` and still send no body.

Alternative considered: change only the controller return type and leave the switch inline. Rejected — the mapper keeps the controller thin and matches the create-complete-case pattern.

## Riesgos / Trade-offs

[Risk] None material. → One MockMvc class for the empty arms plus existing integration tests.

## Testing Strategy

- Shrink the baseline first so `ControllerSignatureArchitectureTest` fails while `getWorkflowTrace` is still `ResponseEntity<Object>`.
- `ManagementWorkflowTraceControllerTest`: 200 trace, empty 400, empty 404, empty 201. The mapper is real.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| getWorkflowTrace leaves the loose baseline | unit | `ControllerSignatureArchitectureTest` |
| Workflow trace keeps 200 trace and empty error statuses | unit | `ManagementWorkflowTraceControllerTest` |

## Regression Strategy

Existing complete-case assertions stay. No test casts the update body to `Object`.

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
