> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The standard `WorkflowDefinition` (id 1, seeded by `V10__seed_workflow_demo_data.sql`)
today models the gestión lifecycle as: Iniciada(1) → En Trámite(2) →
Documentación Completa(3) → [Sin Firmar(7) → Firmada(6)] → **Inscripta(10,
FINAL)**, with Documentación Completa(3) → Archivada(4, FINAL) as the
alternate branch. `WorkflowTraceService.buildTrace` cross-references these
`WorkflowNode`/`WorkflowTransition` rows against a gestión's `Historial` to
compute `nodeStatuses`, and `WorkflowTracker.tsx` renders that trace as the
animated diagram on the main dashboard. See proposal.md — Objetivo for why
this jump from Firmada straight to Inscripta cannot represent the real
circuit (`transicion-de-estados.puml`): "Generar testimonio" → "Ingresar
para inscripción" → (open loop: "¿Volvió observado?" → "Registrar reingreso"
→ "Ingresar para inscripción" again) → "Retirar testimonio".

`MovimientoTestimonio` (`negocio/MovimientoTestimonio.java`,
`repository/MovimientoTestimonioRepository.java`) already exists as a
legacy-migrated entity with `fechaIngreso`, `fechaSalida`,
`fechaInscripcion`, `inscripta` and `numeroCarton` — reachable from a
gestión via `GestionDeEscritura.tramiteList[].getFkIdEscritura()`
`.getTestimonioList()[].getMovimientoTestimonioList()`. No new column or
entity is needed to read it; #832 (`escritura-post-firma-legal-cycle`, not
yet implemented — its `tasks.md` is 0/N) is the change that will start
*writing* these rows through real business actions instead of the generic
CRUD `PUT`. #833 (`gestion-workflow-y-bitacora`, also 0/N) is the change
that will start writing `Historial` entries for validated transitions. This
change's `buildTrace` extension reads whatever rows already exist — it does
not require #832/#833 to be merged first to be implemented and tested with
its own fixtures, but the three new `EstadoDeGestion` only get populated
into a real gestión's `Historial` once #833's transition-writing exists, and
`MovimientoTestimonio` rows only get created by real user action once #832
exists. See Riesgos / Trade-offs for the sequencing implication.

`DtoGestionWorkflowTrace` (`notaire-shared/.../dto/DtoGestionWorkflowTrace.java`)
already has a nested `DtoHistorialEntry` pattern to follow for the new
movements list.

## Goals / Non-Goals

**Goals:**
- Add the three new `EstadoDeGestion` rows and wire them into the standard
  `WorkflowDefinition`'s nodes/transitions, replacing the current
  direct Firmada → Inscripta jump.
- Extend `WorkflowTraceService.buildTrace` to also return the testimonio's
  `MovimientoTestimonio` list for the gestión, without changing the shape of
  `nodes`/`transitions`/`nodeStatuses` already consumed by `WorkflowTracker.tsx`.
- Extend `WorkflowTracker.tsx` to render that list as a secondary timeline on
  the "Testimonio Ingresado a Inscripción" node, with a reingreso counter.

**Non-Goals:**
- Writing `MovimientoTestimonio` rows from a business action (generar,
  ingresar, retirar, reingresar) — that is #832's endpoint surface. This
  change only reads existing rows.
- Validating gestión state transitions against the workflow graph, or
  writing `Historial` entries when a state changes — that is #833's
  `GestionTransitionService`. This change only adds the nodes/states that
  service will validate against once it exists.
- A `volvioObservado` column on `MovimientoTestimonio` — derived instead
  from existing fields (see Decisions).

## Decisions

- **A movement "volvió observado" is derived as `fechaSalida != null &&
  !inscripta`, not a new column.** Alternative considered: add a
  `volvio_observado` boolean to `movimientos_testimonio` — rejected, the
  existing fields already carry this fact (a movement that left the
  registry `fechaSalida` without being inscribed `inscripta = false` is, by
  definition, a returned-observed attempt; the last movement of a
  still-open testimonio has `fechaSalida = null`). Adding a redundant column
  would create a second source of truth that could drift from the dates.
  Reingreso count for the vigente testimonio = count of its movements where
  this predicate holds.
- **Replace the existing Firmada→Inscripta path (`workflow_node` id 6,
  estado 10; `workflow_transition` id 5) with three new nodes/transitions,
  in a new seed migration, rather than leaving node 6 orphaned alongside
  the new nodes.** Alternative considered: keep `workflow_node` id 6
  (estado "Gestión con Escritura Inscripta") as an additional dangling
  FINAL node reachable from nowhere once the new path exists — rejected,
  `WorkflowValidationService`/the workflow editor (CU83) already asserts a
  `WorkflowDefinition` has "at least one final node reachable"; leaving an
  orphaned final node the graph never reaches has no functional harm but
  renders in the workflow editor UI as a disconnected node, confusing an
  administrator with no explanation. `estados_de_gestion` id 10 itself is
  reference data used nowhere else in code (grepped: only the V10 seed
  references it) so removing its `workflow_node`/`workflow_transition` seed
  row is safe; the `EstadoDeGestion` row itself is left in place (Flyway
  data is additive-only; no other row references it, so nothing breaks by
  leaving it unused).
- **New `WorkflowNode`s for the three new states are `INTERMEDIATE`,
  `INTERMEDIATE`, `FINAL`** (Generado → Ingresado a Inscripción → Retirado
  = FINAL), matching the puml where "Retirar testimonio" is the last step
  of the post-firma branch before the outer "Archivar gestión" step common
  to all branches. Non-Goal: modeling "Archivar gestión" itself as reachable
  from "Testimonio Retirado" — the existing workflow definition already
  treats "Documentación Completa → Archivada" as its own branch and does
  not attempt to model archiving as a per-branch terminal step for any of
  today's states either; this change does not introduce a new inconsistency,
  it follows the same simplification already accepted for the rest of the
  graph.
- **`WorkflowTraceService.buildTrace` resolves the testimonio via the first
  `Tramite` with a non-null `fkIdEscritura` whose `testimonioList` is
  non-empty**, mirroring how it already resolves `tipoTramite` from
  `tramites.get(0)`. Alternative considered: aggregate movements across
  every trámite/escritura/testimonio of the gestión — rejected as
  overengineering for this change: the spec's scenarios (see spec.md) all
  describe "a gestión's testimonio" in the singular, matching how the rest
  of `buildTrace` already treats the gestión as having one relevant
  workflow path. Revisit if a future change needs multi-trámite testimonio
  tracking.
- **The new fields live on `DtoGestionWorkflowTrace` as an additive
  `List<DtoMovimientoTestimonioEntry> movimientosTestimonio` (empty/absent
  when there is no testimonio), reusing the existing
  `DtoHistorialEntry`-nested-class pattern.** Alternative considered:
  reuse the full `DtoMovimientoTestimonio` from `notaire-shared` as-is —
  rejected, that DTO carries a full `DtoTestimonio` back-reference the
  trace does not need and the spec's scenario ("fecha de ingreso, fecha de
  salida, si volvió observado") only needs four fields; a lightweight
  nested DTO keeps the trace payload focused, consistent with why
  `DtoHistorialEntry` already exists instead of reusing `DtoHistorial`.

## Riesgos / Trade-offs

- [This change's new states/nodes are inert until #833's transition-writing
  service exists — a real gestión's `Historial` will not actually reach
  "Testimonio Generado" until #833 is implemented, and its
  `MovimientoTestimonio` rows will not exist until #832 is implemented] →
  Mitigation: not mitigated within this change, by design — proposal.md
  states #832/#833 as prerequisites this change depends on but does not
  implement. Testing Strategy below covers this change's own logic (trace
  building, node/transition wiring, UI rendering) against fixtures that
  simulate the post-#832/#833 world, so it is fully verifiable in isolation;
  the end-to-end real-data path only closes once all three changes are
  merged. Implementation of #841 SHOULD be sequenced after #832 and #833
  land, to avoid re-testing this integration by hand twice.
- [Removing `workflow_node` id 6 / `workflow_transition` id 5 changes the
  standard `WorkflowDefinition`'s graph shape for the two demo gestiones
  already seeded by `V10` (ids 1 and 2)] → Mitigation: both demo gestiones
  are seeded at estado 2 and 3 respectively (`En Trámite`,
  `Documentación Completa`), never reaching estado 6/10 in their `Historial`
  — `computeNodeStatuses` already reports any node absent from `Historial`
  as `pending`, so removing the node they never reached does not change
  their computed statuses.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Generar el testimonio de una escritura firmada avanza el estado de la gestión | integration | `WorkflowNodeRepositoryIntegrationTest#shouldExposeTestimonioGeneradoNodeInStandardWorkflow` (or Flyway guard test — see below) |
| Ingresar el testimonio a inscripción avanza el estado de la gestión | integration | same as above, transition-level assertion |
| Retirar el testimonio avanza el estado de la gestión | integration | same as above, transition-level assertion |
| Gestión con testimonio en curso incluye sus movimientos en el trace | unit | `WorkflowTraceServiceTest#shouldIncludeMovimientosTestimonioWhenTestimonioHasMovements` |
| Gestión sin testimonio no incluye movimientos | unit | `WorkflowTraceServiceTest#shouldNotIncludeMovimientosWhenNoTestimonio` |
| Un reingreso agrega un nuevo movimiento sin perder los anteriores | unit | `WorkflowTraceServiceTest#shouldIncludeAllMovimientosInChronologicalOrder` |
| Testimonio con reingresos muestra el conteo en el nodo de inscripción | E2E (Playwright) + component | `workflow-tracker.spec.ts` — `shows reingreso count on inscripción node`; frontend unit/story if the repo has one for `WorkflowTracker` |
| Testimonio sin reingresos no muestra el indicador | E2E (Playwright) | `workflow-tracker.spec.ts` — `does not show reingreso indicator without observations` |
| Gestión cuyo tipo de trámite no tiene el workflow post-firma configurado | unit + E2E | `WorkflowTraceServiceTest#shouldDegradeGracefullyWhenPostFirmaNodesMissing`; `workflow-tracker.spec.ts` — `renders without the post-firma nodes when not configured` |

- New unit tests (`src/test/java/.../unit/`): extend `WorkflowTraceServiceTest`
  with the four cases above, building `GestionDeEscritura` fixtures with a
  `Tramite → Escritura → Testimonio → MovimientoTestimonio` chain in memory
  (no DB needed — `computeNodeStatuses` and the movement-extraction logic
  are pure functions of their inputs).
- New/updated integration tests (`src/test/java/.../integration/`): assert
  the seed migration produces the expected `workflow_node`/
  `workflow_transition` rows for `WorkflowDefinition` id 1 (node count,
  estado ids, no dangling final node) — either as a dedicated
  `Ppg-integration` test alongside `FlywaySchemaValidationIntegrationTest`,
  or a new `WorkflowSeedDataIntegrationTest`.
- New E2E (Playwright): extend `frontend/tests/e2e/workflow-tracker.spec.ts`.
- Coverage impact: new logic (movement extraction, reingreso counting,
  degrade-gracefully branch) fully covered by the table above; expected to
  hold or raise the JaCoCo ratchet floor.

## Regression Strategy

- Existing tests affected: `WorkflowTraceServiceTest` (existing cases for
  `computeNodeStatuses` and the current node/transition/historial shape)
  must keep passing unchanged — this change only adds a new field to the
  DTO and a new code path when a testimonio exists, it does not alter
  `nodes`/`transitions`/`nodeStatuses`/`historial` computation.
- `WorkflowTracker.tsx` existing rendering (node positions, edges, status
  colors) must be visually unchanged for any gestión without the new nodes
  — covered by the "degrades gracefully" scenario.
- `computeNodeStatuses` unit tests for the two V10 demo gestiones (see
  Riesgos above) must keep returning the same statuses after node 6's
  removal.
- Full suite command: `mvn verify -pl backend-api`
- Flyway schema validation guard test: `mvn test -Ppg-integration`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: none — `jpa.MovimientoTestimonioJpaController` and
  `jpa.EstadoDeGestionJpaController` are not touched by this change.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`: `workflow-tracker.spec.ts`.
- Golden path covered: open the dashboard's animated diagram for a gestión
  whose testimonio has movements with reingresos, verify the timeline and
  reingreso counter render on the "Testimonio Ingresado a Inscripción" node.
- Edge / error paths covered: a testimonio with a single movement and no
  reingresos shows no indicator; a gestión whose `WorkflowDefinition` lacks
  the post-firma nodes renders exactly as it does today, no error surfaced.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: yes — `V17__extend_workflow_post_firma_testimonio.sql`,
  adding `EstadoDeGestion` rows 11–13 ("Testimonio Generado", "Testimonio
  Ingresado a Inscripción", "Testimonio Retirado"), adding `workflow_node`
  rows 8–10 for `WorkflowDefinition` id 1, adding `workflow_transition` rows
  linking node 5 (Firmada) → node 8 → node 9 → node 10, and removing the
  now-superseded `workflow_transition` id 5 and `workflow_node` id 6 (see
  Decisions). No column/table changes.
- Deployment order / coupling: single deploy; migration runs before the
  application starts (Flyway `ddl-auto=none`).
- Configuration or `.env` keys to add: ninguna.
- Feature flag: no.
- Smoke test after deploy (Gate 5): `GET /api/v1/gestiones/{id}/workflow-trace`
  for a gestión on the standard workflow returns the three new nodes with
  `pending` status and an empty `movimientosTestimonio`; open the dashboard
  and confirm the animated diagram renders without errors for both a
  gestión on the standard workflow and one on a workflow without the
  post-firma nodes.

## Rollback Strategy

- Revert safe: yes — reverting the application code stops reading/rendering
  `movimientosTestimonio` (back to today's behavior); the DTO field and
  seed rows written during the period remain but are simply unread.
- Database rollback: not required for a code revert. If the seed rows
  themselves must be undone, a new forward migration restores
  `workflow_node` id 6 / `workflow_transition` id 5 and removes rows 8–10 —
  never edit `V17` in place.
- Data written under the new behavior after revert: any `Historial` entries
  that reached the new estados (once #833 exists) remain valid rows
  referencing an `EstadoDeGestion` that is no longer wired into the active
  `WorkflowDefinition` graph — they stay readable in the bitácora (CU13),
  just no longer reachable from the trace's node list until re-deployed.
- Blast radius if rollback is delayed: bajo — this change does not alter
  how `Historial` is written (that is #833's service) or how
  `MovimientoTestimonio` is created (that is #832's service); a delayed
  rollback only affects the read-only trace/diagram, not data integrity.

## Migration Plan

1. Merge and deploy #832 (`escritura-post-firma-legal-cycle`) — provides the
   business actions that will populate `MovimientoTestimonio` going forward.
2. Merge and deploy #833 (`gestion-workflow-y-bitacora`) — provides
   `GestionTransitionService`, the validation/`Historial`-writing path this
   change's new states plug into.
3. Merge and deploy this change (#841): seed the three new states/nodes,
   extend `buildTrace`, extend `WorkflowTracker.tsx`.
4. After step 3, any gestión processed through #832's testimonio actions
   will have its state transitions validated against the extended graph
   (via #833) and its movements rendered in the trace (via this change).

Steps 1–2 are prerequisites, not part of this change's own tasks.md — they
are tracked and implemented independently under their own Issues.

## Open Questions

Ninguna.
