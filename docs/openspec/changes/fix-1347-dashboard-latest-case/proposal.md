# Dashboard hero shows the newest case with a workflow; no dead 'view all' button

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1347 |
| Use Case | RF-23 – Saber el estado de un trámite; CU14 – Consultar estado gestión; CU70/CU71 |
| Branch | `fix/1347_dashboard_latest_case` |
| Gate 1 status | draft |

## Objetivo

The dashboard hero traced the first management in backend order (seed management 1001), the oldest case. #1396 kept that because the newest test and draft cases have no workflow (their trace answers 400). The 'Ver todos los servicios' button had no action, and the fallback subtitle was the English literal `Management #n`.

## What Changes

- `hooks/useGestionWorkflow.ts`: `findLatestTracedGestion()` reads the 20 newest managements (`GET /gestiones?page=0&size=20&sort=idManagement,desc`) and requests their traces in parallel batches of 5, newest first, keeping the first that has a workflow; `useLatestTracedGestion()` wraps it (staleTime 30 s).
- New `components/workflow/WorkflowHero.tsx` (moved out of the page): shows that case, or the searched number; the subtitle reads `#<number> — <header>` (translated fallback `dashboard.workflow.managementFallback`); `data-management-id` names the shown case; an empty state with a link to /dashboard/gestiones when none of the 20 newest has a workflow; the status pill uses theme tokens.
- `app/dashboard/page.tsx`: the dead 'Ver todos los servicios' button is removed (the module grid below already lists every module, and the sidebar too); i18n `dashboard.viewAll` removed.
- i18n `dashboard.workflow.managementFallback`, `noRecentWorkflow`, `openGestiones` in es and en.
- Vitest `dashboard-hero.test.tsx`; Playwright TS-0035 (2 new tests); CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The dashboard hero shows the newest management that has a workflow | #1347, RF-23 | Made explicit |
| Every control does something | #1336, WCAG 4.1.2 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-dashboard`: The dashboard landing page.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | WorkflowHero, useGestionWorkflow, dashboard page, i18n |
| `testing` | yes | Playwright TS-0035 |
| `backend-api` | no | GET /gestiones (sorted page) and GET /gestiones/{id}/workflow-trace already exist |

### Surface area

- Route: /dashboard
- API: GET /api/v1/gestiones?page=0&size=20&sort=idManagement,desc, GET /api/v1/gestiones/{id}/workflow-trace (unchanged contract)

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
