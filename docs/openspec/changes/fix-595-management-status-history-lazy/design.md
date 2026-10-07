# Design

## Context

Issue #595 inventories 29 explicit EAGER mappings across 17 entities, four of them `@OneToMany` collections. This one is the only collection that grows with day-to-day use (one row per state change) and has no external reader besides deletion.

## Goals / Non-Goals

Goal: remove the unbounded fan-out from management status. Non-goals: the other EAGER mappings (Procedure's five `@ManyToOne`, Budget.paymentList, DocumentType.procedureTemplateList, Concept.budgetTemplateList), which stay in #595.

## Decisions

Explicit `FetchType.LAZY` (instead of relying on the default) so the intent is visible next to the other mappings. No fetch joins added: no caller needs the collection outside deletion.

## Riesgos / Trade-offs

A future reader outside a transaction gets `LazyInitializationException` (open-in-view is off); the integration test documents the contract.

## Testing Strategy

Integration test (H2) observed failing first (collection loaded on find and list).

## Regression Strategy

Full backend suite (2069 tests) incl. `HistoryDeleteIntegrationTest` and `ManagementStatusReferentialIntegrityTest`; Playwright TS-0024, TS-0028, TS-0060, TS-0071, TS-0092 and carpetas-de-tramite (69 passed) against this backend on PostgreSQL, no `LazyInitializationException` in the log.

## Playwright Strategy

No UI change; Playwright not affected.

## Deployment Strategy

None; mapping change only.

## Rollback Strategy

Revert the commit.
