> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Post-#1881 person R2 (R2=38). Audit is next (~8 edges).

## Goals / Non-Goals

**Goals:** Clear ~8 audit R2 edges without behaviour change; keep controller R5-clean
**Non-Goals:** Registrationdraft/history R2

## Decisions

Move AuditRecordService to adapter with Spring Data; delete AuditRecordRepositoryPort; add AuditRecordOpsPort (DTO/PageResult, no Spring Data on port); AuditAspect injects relocated helper

## Riesgos / Trade-offs

Controller / aspect / unit test import path updates

## Testing Strategy

AuditRecordServiceTest, AuditRecordControllerTest, AuditAspectTest, HexagonArchitectureTest

## Regression Strategy

Audit REST unchanged

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
