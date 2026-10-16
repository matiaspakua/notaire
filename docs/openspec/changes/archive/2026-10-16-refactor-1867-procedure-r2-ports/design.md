> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Post-#1866 document R2 (R2=85). Procedure is next.

## Goals / Non-Goals

**Goals:** Clear ~17 procedure R2 edges without behaviour change
**Non-Goals:** RegistrationDraftService; full domain.procedure rewrite

## Decisions

Move helpers to adapter; delete entity ports; Spring Data in OpsAdapters; keep ProcedureLookupPort

## Riesgos / Trade-offs

ProcedureOpsAdapter / ProcedureTypeOpsAdapter / ProcedureTemplateOpsAdapter rewiring

## Testing Strategy

ProcedureFolderServiceTest, RequiredDocumentationOnConfirmServiceTest, HexagonArchitectureTest

## Regression Strategy

Procedure REST unchanged

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
