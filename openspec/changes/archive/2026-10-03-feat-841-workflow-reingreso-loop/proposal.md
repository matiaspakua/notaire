# Represent post-firma reingreso loop in workflow tracker (#841)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #841 |
| Use Case | CU83 – Definir Workflow de Estados y Transiciones; CU06 – Firmar escritura; CU07 – Generar testimonio; CU11 – Ingresar para inscripción; CU44 – Reingresar testimonio |
| Branch | `cursor/feat-841-workflow-reingreso-loop-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal stockpile); implement after queue through #799/#800/#805 |

## Objetivo

`WorkflowNode` is 1:1 with `ManagementStatus` (`WorkflowNode.fkEstadoDeGestionId`),
so the animated dashboard tracker cannot represent the unbounded post-firma
reingreso loop from `transicion-de-estados.puml`. Issues #832 and #833 are
already **CLOSED** (legal cycle + bitácora/transitions), but the tracker still
cannot show N reingresos without flattening them into mutually exclusive nodes.
This change chooses strategy **(b)** from #841: keep the node graph for linear
post-firma states, and surface `TestimonyMovement` as a secondary timeline.

## What Changes

- Flyway seed: add three `management_statuses` rows (Testimonio Generado /
  Ingresado a Inscripción / Retirado) and extend the standard
  `WorkflowDefinition` (V10) so Firmada no longer jumps straight to Inscripta.
- Extend `WorkflowTraceService.buildTrace` /
  `DtoManagementWorkflowTrace` with a chronological
  `testimonyMovements` list (derived `returnedObserved` =
  `dateExit != null && !registered`).
- Extend `WorkflowTracker.tsx` / `WorkflowHero` to render that list as a
  secondary timeline on the inscripción node, with a reingreso count when > 0.
- Cross-link CU83 and the archived `escritura-post-firma-legal-cycle` docs.

Supersedes the stale in-tree draft
`openspec/changes/gestion-workflow-reingreso-testimonio/` (Spanish package
names, #832/#833 still marked unimplemented). Implement workers MUST use
**this** change name.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Post-firma linear steps (generado → ingresado → retirado) are distinct management statuses on the standard workflow | CU83; CU07; CU11; #841 AC strategy | New (catalog + seed) |
| Reingreso is an unbounded **event**, not a node — each attempt is a `TestimonyMovement` row | CU44; #832 design; #841 AC (b) | Made explicit in tracker |
| Tracker MUST NOT regress the existing nodeStatuses / Historial rendering | CU83; FRONTEND-WORKFLOW-TRACKER | Unchanged contract + additive field |
| Reingreso count = movements where exit without inscription | #841 design | Made explicit (derived, no new column) |

## Capabilities

### New Capabilities

- `workflow-reingreso-loop-tracker`: Post-firma statuses on the standard
  workflow plus secondary `TestimonyMovement` timeline / reingreso count on
  the animated tracker.

### Modified Capabilities

- (none — new capability; may later sync into `gestion-workflow-transicion`
  / `testimonio-movimiento-inscripcion` on archive)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Flyway seed; `WorkflowTraceService` movement extraction; unit/integration tests |
| `frontend` | yes | `WorkflowTracker.tsx`, types, Playwright |
| `frontend-swing` | no | removed |
| `notaire-shared` | yes | `DtoManagementWorkflowTrace` + nested movement entry |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `ManagementStatus`, `WorkflowNode`, `WorkflowTransition`,
  `TestimonyMovement` (read-only), `DeedManagement` graph for traversal
- Endpoints: `GET /api/v1/gestiones/{id}/workflow-trace` — **additive** field
  (not BREAKING)
- Database (Flyway `V{n}`): **yes** — `V41__extend_workflow_post_firma_testimony.sql`
  (tip on main at implement was V40): insert statuses 11–13; adjust standard
  workflow nodes / transitions (replace Firmada→Inscripta jump)
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows ADR-014 (workflow as data), repository/ports over legacy `jpa`, and
Flyway as schema source of truth. Strategy (b) avoids modeling unbounded loops
as exclusive nodes. No new ADR required (extends ADR-014).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md` | Standard workflow covers post-firma statuses; reingreso shown as movements |
| `docs/200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md` | Secondary timeline + reingreso counter |
| Cross-ref in archived / main spec for `escritura-post-firma-legal-cycle` / `testimonio-movimiento-inscripcion` | Point to tracker strategy (b) |
| `CHANGELOG.md` | User-visible tracker improvement under `[Unreleased]` |

## Out of Scope

- Re-implementing #832 movement writers or #833 transition/bitácora writers
  (already shipped).
- Strategy (a) alone (flattening reingreso into N exclusive nodes).
- Adding a persisted `volvio_observado` / `returned_observed` column.
- Modeling "Archivar gestión" as reachable from every post-firma terminal.
