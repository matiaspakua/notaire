## Context

Final #577 loose-return tip.

## Goals / Non-Goals

**Goals:** `ResponseEntity<DeedResponse>`; empty baseline file.

**Non-Goals:** Closing #577.

## Testing Strategy

`AuxiliaryProtocolStartDeedControllerTest` + `ControllerSignatureArchitectureTest`.

## Decisions

`AuxiliaryProtocolStartDeedWebMapper` + advice for 400/404 message bodies.

## Riesgos / Trade-offs

None material.

## Regression Strategy

`AuxiliaryProtocolControllerTest` integration PRESERVE.

## Playwright Strategy

n/a

## Deployment Strategy

Next release.

## Rollback Strategy

Revert PR.

## Migration Plan

None.

## Open Questions

None.
