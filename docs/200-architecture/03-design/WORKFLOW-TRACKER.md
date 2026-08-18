# Workflow Tracker — Animated Gestión Workflow on the Dashboard

**Issue:** [#453](https://github.com/matiaspakua/notaire/issues/453) (parent #436)
**Use Cases:** CU70 – Definir Workflow de Estados de Gestión, CU71 – Definir Transiciones entre Estados
**Concept:** `frontend/poc_motion_js/poc.md`

## Overview

The dashboard landing page (`/dashboard`) renders an animated, interactive
visualization of a gestión's workflow: the directed graph of Estados de
Gestión defined for the gestión's tipo de trámite, overlaid with the gestión's
real historial so each node shows live progress (completed → in progress →
pending).

## Backend

### Endpoint

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/gestiones/{id}/workflow-trace` | Aggregated trace: workflow definition, nodes, transitions, historial, per-node statuses |

Implemented by `GestionController` + `WorkflowTraceService.buildTrace()`.
Returns **400** with `{ "error": ... }` when the gestión does not exist, has
no trámites, or its tipo de trámite has no workflow definition assigned.

### Node status computation

`WorkflowTraceService.computeNodeStatuses()` sorts historial by `fecha` and
collects the distinct estado IDs in chronological order:

- node's estado is the **latest** distinct estado → `in_progress`
- node's estado appears **earlier** in the historial → `completed`
- node's estado never appears → `pending`

### Demo seed data

Flyway `V10__seed_workflow_demo_data.sql` (and formerly `init-db/02-data.sql`,
now archived at `docs/archive/init-db/`) seed:

- "Workflow de Gestión Estándar" — 7 nodes covering Iniciada → Inscripta with
  a fork at *Documentación Completa* (→ Escritura Sin Firmar or → Archivada)
- The workflow assigned to tipos de trámite Compraventa, Donación, Hipoteca
- Two sample gestiones (1001, 1002) with trámites and historial

## Frontend

### Components

| File | Role |
|------|------|
| `frontend/src/app/dashboard/page.tsx` (`WorkflowHero`) | Hero section: trace of the current gestión + search by número de referencia |
| `frontend/src/components/motion/WorkflowTracker.tsx` | SVG graph: DAG layout, animated edges, traveling dots, node modal |
| `frontend/src/hooks/useGestionWorkflow.ts` | React Query hook for the trace endpoint |
| `frontend/src/hooks/useGestiones.ts` (`useGestionByNumero`) | Lookup gestión by número for the search form |

### Visualization

- **Layout:** longest-path DAG layering (handles forks and re-convergence),
  rows centered horizontally.
- **Animations** (all gated by `prefers-reduced-motion`): edge draw-in via
  `motion.path` pathLength, traveling dots via SMIL `<animateMotion>` on
  active edges, pulse halo on the in-progress node, spring modal.
- **Interaction:** nodes are keyboard-focusable buttons; clicking (or
  Enter/Space) opens a detail modal with incoming/outgoing transitions and
  the matching historial entries; Escape closes.
- **Styling:** theme tokens only (`@/theme/tokens`), Apple easing curve.

## API serialization fixes (latent bugs surfaced by the seed data)

With `spring.jpa.open-in-view=false`, endpoints returning raw JPA entities
failed (HTTP 500, "Failed to write request") as soon as related workflow data
existed. Fixed by returning read-model DTOs:

| Endpoint | Fix |
|----------|-----|
| `GET /gestiones`, `/gestiones/{id}`, `/gestiones/numero/{n}` | `DtoGestionSummary` via `GestionQueryService` (`@Transactional(readOnly=true)`) |
| `GET /historial`, `/historial/{id}`, `/historial/gestion/{id}`, `/gestiones/{id}/estado-actual` | `DtoHistorialSummary` (the entity's eager gestión back-reference is circular) |
| `GET /tipo-tramite` (+ `/search`, `/{id}`) | `@Transactional(readOnly=true)` so `getDto()` can read the lazy `workflowDefinition` |

`TipoDeTramite.getWorkflowDefinition()` is now `@JsonIgnore`, matching the
class's other lazy associations; API consumers use `workflowDefinitionId` /
`workflowDefinitionNombre` from `DtoTipoDeTramite`.

## Endpoint → UI traceability

| Endpoint | UI caller |
|----------|-----------|
| `GET /api/v1/gestiones/{id}/workflow-trace` | Dashboard `WorkflowHero` (default: latest gestión; after search: resolved gestión) |
| `GET /api/v1/gestiones/numero/{numero}` | Dashboard `WorkflowHero` search form ("Número de referencia") |

## Tests

| Layer | Location |
|-------|----------|
| Unit | `backend-api/src/test/java/.../unit/WorkflowTraceServiceTest.java` |
| Integration (H2) | `backend-api/src/test/java/.../integration/WorkflowTraceApiH2IntegrationTest.java` (`@RequirementCoverage({"CU70","CU71"})`) |
| E2E | `frontend/tests/e2e/workflow-tracker.spec.ts` |
