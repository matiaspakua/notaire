> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after ProcedureFolder.

## Goals / Non-Goals

**Goals:** Clear two R1 edges without REST or behaviour change.
**Non-Goals:** ProcedureController; CHANGELOG.

## Decisions

Adapter wraps ProcedureTemplateRepositoryPort; response view lives on the ops port.

## Riesgos / Trade-offs

Low — composite-key CRUD surface.

## Testing Strategy

ProcedureTemplateControllerTest updated to port mocks; ArchUnit baseline shrink.

## Regression Strategy

Existing CU55 unit/integration tests.

## Playwright Strategy

n/a — no UI surface

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
