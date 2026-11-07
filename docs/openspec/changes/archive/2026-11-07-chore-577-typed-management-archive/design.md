> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Issue #577 requires controller handler returns to stop using `ResponseEntity<Object>`. `archiving` is the smallest Management write that still needs one advice: success is `DtoArchivedManagement`, NotFound is an empty 404, and waiting folders are a 409 map. `create` can be typed with empty error bodies; `transition` is a single DTO behind `Object`. Login and logout do not share an exception advice.

## Goals / Non-Goals

**Goals:**

- Declare `ResponseEntity<DtoArchivedManagement>` on `archiving`.
- Map NotFound to an empty 404 and waiting folders to `{message, waitingFolderNumbers}` 409 through `ManagementArchiveExceptionHandler`.
- Remove `ManagementController#archiving` from `controller-loose-returns.txt`.
- Declare the Write set for `aisdlc scope`.

**Non-Goals:**

- Closing #577.
- Typing `create`, `transition`, or other Management methods.
- Changing archive persistence or the HTTP contract.

## Decisions

Throw `ManagementArchiveNotFoundException` and `ManagementArchiveConflictException` from the controller switch on `ArchiveResult`, matching RegistrationDraft and ProcedureFolder. The handler is `@RestControllerAdvice(assignableTypes = ManagementController.class)` at highest precedence so `GlobalExceptionHandler`'s `Exception` catch-all does not replace the 409 map with a 500. `confirmado` stays a request parameter forwarded to the port. Standalone MockMvc tests register the advice; without it those throws become 500.

## Riesgos / Trade-offs

None material. Stored data, auth, and the JSON contract stay the same. A missing advice registration in a new MockMvc test would hide 404/409 as 500; this tip wires the handler in `ManagementArchiveControllerTest`. The full Spring context picks up the `@RestControllerAdvice` bean.

## Testing Strategy

- `ManagementArchiveControllerTest`: 200 DTO body, empty 404, and 409 `message` plus `waitingFolderNumbers`, including `confirmado=true`.
- `ControllerSignatureArchitectureTest`: `ManagementController#archiving` is absent from the baseline and is not loose.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| archiving leaves the loose baseline | unit | `ControllerSignatureArchitectureTest` |
| Missing management and waiting folders keep their HTTP bodies | unit | `ManagementArchiveControllerTest` |

- New unit tests: `ManagementArchiveControllerTest`
- New integration tests: none (existing `ManagementArchiveIntegrationTest` is PRESERVE)
- TDD: shrink the baseline and run `ControllerSignatureArchitectureTest` before the return type changes

## Regression Strategy

Existing CU16 archive assertions stay. Integration archive flows are PRESERVE: status codes and bodies do not change.

## Playwright Strategy

n/a — no UI change in this tip (CU16 screens already covered).

## Deployment Strategy

Next release. No migration and no config change.

## Rollback Strategy

Revert the pull request.

## Migration Plan

None.

## Open Questions

None.
