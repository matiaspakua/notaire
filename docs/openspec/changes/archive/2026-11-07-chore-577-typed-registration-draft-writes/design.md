> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Issue #577 requires controller handler returns to stop using `ResponseEntity<Object>`. The four RegistrationDraft write methods share `DraftResult` (Ok, NotFound, Invalid) and the same error bodies, so one `@RestControllerAdvice` covers the tip.

## Goals / Non-Goals

**Goals:**

- Declare `ResponseEntity<DtoRegistrationDraft>` on generate, submit, observe, and recordRegistration.
- Map NotFound to an empty 404 and Invalid to `{"message"}` 400 through `RegistrationDraftExceptionHandler`.
- Remove the four lines from `controller-loose-returns.txt`.
- Declare the Write set for `aisdlc scope`.

**Non-Goals:**

- Closing #577.
- Changing query `getById`, persistence, or the HTTP contract.
- Touching ProcedureFolder putOnHold.

## Decisions

Throw `RegistrationDraftNotFoundException` and `RegistrationDraftBadRequestException` from the controller switch on `DraftResult`, matching ProcedureFolder putOnHold. The handler is `@RestControllerAdvice(assignableTypes = RegistrationDraftController.class)` at highest precedence so the global handler does not replace the `{"message"}` body. Success stays `CreatedResponses` (201 + Location) for generate and `200` with the DTO for the three updates. Standalone MockMvc tests register the advice; without it those throws become 500.

## Riesgos / Trade-offs

None material. Stored data, auth, and the JSON contract stay the same. A missing advice registration in a new MockMvc test would hide 400/404 as 500; this tip wires the handler in `RegistrationDraftControllerTest`.

## Testing Strategy

- `RegistrationDraftControllerTest`: 201/200 DTO bodies plus 400 `message` and empty 404 on all four writes.
- `ControllerSignatureArchitectureTest`: the four methods are absent from the baseline and are not loose.

## Regression Strategy

Existing CU82 controller assertions stay. Integration and Playwright registration-draft flows are PRESERVE: status codes and bodies do not change.

## Playwright Strategy

n/a — no UI change in this tip (CU82 screens already covered).

## Deployment Strategy

Next release. No migration and no config change.

## Rollback Strategy

Revert the pull request.

## Migration Plan

None.

## Open Questions

None.
