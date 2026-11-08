> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 ADR-030. Pattern: FE deeds #1831 / PR #1835.

## Goals / Non-Goals

**Goals:** Shrink two payment-related eslint suppressions; keep REST behaviour identical.
**Non-Goals:** No OpenAPI or backend changes; do not migrate usePaymentReceipt / useReports.

## Decisions

Mirror deed hexagon; keep PaymentStatus wire strings; duck-type 409 on payments page instead of importing ApiError.

## Riesgos / Trade-offs

Create must send idBudget (PaymentRequest); update omits idBudget server-side but client may still send Partial.

## Testing Strategy

Vitest use-cases + gateway + architecture-boundaries.

## Regression Strategy

Payments list, budget/date filters, create with balance check (409), CRUD.

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
