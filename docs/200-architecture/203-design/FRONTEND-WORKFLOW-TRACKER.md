# Workflow Tracker — Animated Gestión Workflow on the Dashboard

**Issue:** [#453](https://github.com/matiaspakua/notaire/issues/453) (parent #436)
**Use Cases:** [CU83 – Definir Workflow de Estados y Transiciones](../../100-business/102-use-cases/CU83%20%E2%80%93%20Definir%20Workflow%20de%20Estados%20y%20Transiciones.md)
**Concept:** [archived concept](../../000-archive/200-architecture/WORKFLOW-TRACKER-CONCEPT-poc_motion_js.md) (original design chat; superseded by this document)

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
| GET | `/api/v1/gestiones/{id}/workflow-trace` | Aggregated trace: workflow definition, nodes, transitions, history, per-node statuses, optional `testimonyMovements`. Also the **legal-next** contract for CU83 (#804): filter `transitions` by current node → valid destinations for `POST /{id}/transition`. |

Implemented by `ManagementController` + `WorkflowTraceService.buildTrace()`.
Returns **400** with `{ "error": ... }` when the gestión does not exist, has
no trámites, or its tipo de trámite has no workflow definition assigned.
Generic PUT status mutations are rejected (#804); the gestiones UI already uses
`/transition` with destinations from this trace.

### Node status computation

`WorkflowTraceService.computeNodeStatuses()` sorts history by date and
collects the distinct status IDs in chronological order:

- node's status is the **latest** distinct status → `in_progress`
- node's status appears **earlier** in the history → `completed`
- node's status never appears → `pending`

### Demo seed data

Flyway `V10__seed_workflow_demo_data.sql` seeds the base graph; `V41__extend_workflow_post_firma_testimony.sql`
(#841) extends it:

- "Workflow de Gestión Estándar" — nodes covering Iniciada → Firmada →
  Testimonio Generado → Testimonio Ingresado a Inscripción → Testimonio
  Retirado (FINAL), with a fork at *Documentación Completa* → Archivada
- Statuses 11–13 added; status id 10 (Inscripta) remains as inert catalog
- The workflow assigned to procedure types Compraventa, Donación, Hipoteca
- Two sample gestiones (1001, 1002) with procedures and history

### Testimony movements / reingreso loop (#841, strategy b)

`buildTrace` also returns `testimonyMovements` (empty when none): chronological
`TestimonyMovement` rows for the first procedure→deed→testimony chain, with
`returnedObserved = dateExit != null && !registered`.

The animated tracker (`WorkflowTracker.tsx`) keeps the mutually exclusive node
graph for linear post-signing statuses and shows a **secondary timeline** plus
a reingreso count badge on the inscription node (`statusManagementId === 12`)
when the returned-observed count is greater than zero. This does not model the
unbounded reingreso loop as exclusive workflow nodes.
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
| Integration (H2) | `backend-api/src/test/java/.../integration/WorkflowTraceApiH2IntegrationTest.java` (`@RequirementCoverage({"CU83"})`) |
| E2E | `testing/e2e/tests/workflow-tracker.spec.ts` |
