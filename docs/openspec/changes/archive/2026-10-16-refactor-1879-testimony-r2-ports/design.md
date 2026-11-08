> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Post-#1867 procedure R2 (R2=68). Testimony is next (~12 edges).

## Goals / Non-Goals

**Goals:** Clear ~12 testimony R2 edges without behaviour change
**Non-Goals:** Notebook/person R2; rewriting Manage/Query ports already on DTOs

## Decisions

Move helpers to adapter; delete dead entity ports + PersistenceAdapters; keep TestimonyOpsPort / TestimonyMovementOpsPort / TestimonyReentry

## Riesgos / Trade-offs

TestimonyOpsAdapter / TestimonyMovementOpsAdapter import path updates only

## Testing Strategy

TestimonyGenerationServiceTest, TestimonyVerificationServiceTest, TestimonyMovementServiceTest, HexagonArchitectureTest

## Regression Strategy

Testimony REST unchanged

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
