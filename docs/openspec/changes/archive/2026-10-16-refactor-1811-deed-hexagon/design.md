> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after Item (#1809).

## Goals / Non-Goals

**Goals:** ['ports-only DeedController', 'clear R1 edges']
**Non-Goals:** ['CHANGELOG Unreleased', 'PersonController']

## Decisions

['PageResult avoids Spring Data on ports; canonical status and folio link live in OpsAdapter; signing delegates to DeedSigningService']

## Riesgos / Trade-offs

['SimpleControllersTest and AuditPersonalDataReadTest construct/reflect DeedController']

## Testing Strategy

HexagonArchitectureTest; SimpleControllersTest DeedController; DeedSigningControllerIntegrationTest

## Regression Strategy

Bruno deeds; characterization tests

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
