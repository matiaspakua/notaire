> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #797, Use Case CU01 – Preparar Presupuesto (#154); CU39 – Crear Plantilla Presupuesto; CU71 – Gestión de Items. #834 added loading a template into an existing budget; #797 asks for the same from the create form.

## Goals / Non-Goals

**Goals:** One step from the create form to a budget with its template items.
**Non-Goals:** A backend endpoint that creates budget and items atomically; inline editing of item lines in the create form (CU71 Items screen already edits them).

## Decisions

1. Compose the two existing calls in the hook instead of adding an endpoint: the budget id is needed first and the template step can legitimately fail without invalidating the budget. Rejected: a combined transactional endpoint, which would duplicate the template rules.
2. The amount stays a separate stored field (property value) and the total is derived from items by the CU47 summary; no schema change.

## Riesgos / Trade-offs

- If the second call fails after the first succeeded the budget exists without items; the hook reports it and the user can retry from the items dialog.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| A type with template is selected | e2e | `testing/e2e/tests/presupuesto-plantilla.spec.ts` |
| No type is selected | unit | `usePresupuestos.test.tsx` |
| A type with template is selected (hook) | unit | `usePresupuestos.test.tsx` |
| A type without template is selected | unit | `usePresupuestos.test.tsx` |

- New unit tests (`src/test/java/.../unit/`): `usePresupuestos.test.tsx`
- New integration tests: existing items-desde-plantilla integration tests cover the endpoint
- Coverage impact: positive: frontend only; the Vitest floors are unchanged or higher

## Regression Strategy

- Existing tests affected: none
- Full suite command: `npx vitest run; Playwright presupuesto-plantilla`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit; creating a presupuesto with a type shows its template items

## Rollback Strategy

- Revert the PR.
