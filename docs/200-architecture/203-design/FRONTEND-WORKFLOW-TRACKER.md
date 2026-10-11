# Workflow Tracker — Animated management workflow on the dashboard

**Issue:** [#453](https://github.com/matiaspakua/notaire/issues/453) (parent #436)
**Use Cases:** [CU83 – Definir Workflow de Estados y Transiciones](../../100-business/102-use-cases/CU83%20%E2%80%93%20Definir%20Workflow%20de%20Estados%20y%20Transiciones.md)
**Concept:** [archived concept](../../000-archive/200-architecture/WORKFLOW-TRACKER-CONCEPT-poc_motion_js.md) (original design chat; superseded by this document)

## Overview

The dashboard landing page (`/dashboard`) renders an animated, interactive
visualization of a management's workflow: the directed graph of management
statuses defined for the management's procedure type, overlaid with the
management's real history so each node shows live progress (completed → in
progress → pending).

## Backend

### Endpoint

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/managements/{id}/workflow-trace` | Aggregated trace: workflow definition, nodes, transitions, history, per-node statuses, optional `testimonyMovements`. Also the **legal-next** contract for CU83 (#804): filter `transitions` by current node → valid destinations for `POST /{id}/transition`. |

Implemented by `ManagementController` + `WorkflowTraceService.buildTrace()`.
Returns **400** with `{ "error": ... }` when the management does not exist, has
no procedures, or its procedure type has no workflow definition assigned.
Generic PUT status mutations are rejected (#804); the managements UI already uses
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

- "Workflow de Gestión Estándar" — seeded nodes from `Iniciada` through
  `En Tramite`, `Documentacion Completa`, `Gestion con Escritura Sin Firmar`,
  and `Gestion con Escritura Firmada`, then `Testimonio Generado`,
  `Testimonio Ingresado a Inscripcion`, and `Testimonio Retirado` (FINAL).
  A fork goes from `Documentacion Completa` to `Archivada`.
- Statuses 11–13 added; status id 10 (`Gestion con Escritura Inscripta`) remains as inert catalog
- The workflow assigned to procedure types `Compraventa`, `Donacion`, and `Hipoteca`
- Two sample managements (1001, 1002) with procedures and history

### Testimony movements / re-entry loop (#841, strategy b)

`buildTrace` also returns `testimonyMovements` (empty when none): chronological
`TestimonyMovement` rows for the first procedure→deed→testimony chain, with
`returnedObserved = dateExit != null && !registered`.

The animated tracker (`WorkflowTracker.tsx`) keeps the mutually exclusive node
graph for linear post-signing statuses and shows a **secondary timeline** plus
a re-entry count badge on the inscription node (`statusManagementId === 12`)
when the returned-observed count is greater than zero. This does not model the
unbounded re-entry loop as exclusive workflow nodes.
## Frontend

### Components

| File | Role |
|------|------|
| `frontend/src/app/dashboard/page.tsx` (`WorkflowHero`) | Hero section: trace of the current management + search by reference number |
| `frontend/src/components/motion/WorkflowTracker.tsx` | SVG graph: DAG layout, animated edges, traveling dots, node modal |
| `frontend/src/hooks/useManagementWorkflow.ts` | React Query hook for the trace endpoint |
| `frontend/src/hooks/useManagements.ts` (`useManagementByNumber`) | Look up a management by number for the search form |

### Visualization

- **Layout:** longest-path DAG layering (handles forks and re-convergence),
  rows centered horizontally.
- **Animations** (all gated by `prefers-reduced-motion`): edge draw-in via
  `motion.path` pathLength, traveling dots via SMIL `<animateMotion>` on
  active edges, pulse halo on the in-progress node, spring modal.
- **Interaction:** nodes are keyboard-focusable buttons; clicking (or
  Enter/Space) opens a detail modal with incoming/outgoing transitions and
  the matching history entries; Escape closes.
- **Styling:** theme tokens only (`@/theme/tokens`), Apple easing curve.

## API serialization fixes (latent bugs surfaced by the seed data)

With `spring.jpa.open-in-view=false`, endpoints returning raw JPA entities
failed (HTTP 500, "Failed to write request") as soon as related workflow data
existed. Fixed by returning read-model DTOs:

| Endpoint | Fix |
|----------|-----|
| `GET /managements`, `/managements/{id}`, `/managements/number/{n}` | `DtoManagementSummary` via `ManagementQueryService` (`@Transactional(readOnly=true)`) |
| `GET /history`, `/history/{id}`, `/history/management/{id}`, `/managements/{id}/current-status` | `DtoHistorySummary` (the entity's eager management back-reference is circular) |
| `GET /procedure-types` (+ `/search`, `/{id}`) | `@Transactional(readOnly=true)` so `getDto()` can read the lazy `workflowDefinition` |

`ProcedureType.getWorkflowDefinition()` is now `@JsonIgnore`, matching the
class's other lazy associations; API consumers use `workflowDefinitionId` /
`workflowDefinitionName` from `DtoProcedureType`.

## Endpoint → UI traceability

| Endpoint | UI caller |
|----------|-----------|
| `GET /api/v1/managements/{id}/workflow-trace` | Dashboard `WorkflowHero` (default: latest management; after search: the resolved management) |
| `GET /api/v1/managements/number/{number}` | Dashboard `WorkflowHero` search form (reference number) |

## Tests

| Layer | Location |
|-------|----------|
| Unit | `backend-api/src/test/java/.../unit/WorkflowTraceServiceTest.java` |
| Integration (H2) | `backend-api/src/test/java/.../integration/WorkflowTraceApiH2IntegrationTest.java` (`@RequirementCoverage({"CU83"})`) |
| E2E | `testing/e2e/tests/workflow-tracker.spec.ts` |
