> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#577 putOnHold uses ResponseEntity<Object>.

## Goals / Non-Goals

**Goals:** ['Type putOnHold', 'Shrink baseline by 1', 'Declare Write set']
**Non-Goals:** ['Closing #577']

## Decisions

['BadRequest keeps {"message"} Map body via advice']

## Riesgos / Trade-offs

['None material']

## Testing Strategy

['ControllerSignatureArchitectureTest']

## Regression Strategy

['Integration/E2E CU85 hold flows PRESERVE']

## Playwright Strategy

n/a — no UI change in this tip (CU85 already covered).

## Deployment Strategy

Next release.

## Rollback Strategy

Revert PR.

## Migration Plan

None.

## Open Questions

None.
