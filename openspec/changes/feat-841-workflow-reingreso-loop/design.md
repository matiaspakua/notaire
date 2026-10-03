> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

On `origin/main` tip, English domain names apply: `ManagementStatus`,
`WorkflowNode`, `TestimonyMovement` (`dateEntry` / `dateExit` /
`dateRegistration` / `registered`), `DtoManagementWorkflowTrace`,
`WorkflowTraceService.buildTrace`. Table `management_statuses` has 10 seed
rows (V2 via V26 rename). V10 standard workflow is still Alta → … → Firmada →
Inscripta (FINAL) with Archivada alternate — no post-firma testimonio nodes.
#832 and #833 are **CLOSED**; movement rows and history transitions can exist
from real actions. The stale draft
`openspec/changes/gestion-workflow-reingreso-testimonio/` chose strategy (b)
but cites pre-rename packages and unimplemented deps — this design refreshes
that decision for current main.

## Goals / Non-Goals

**Goals:**
- Decide and document strategy **(b)**: linear post-firma statuses on the node
  graph + `TestimonyMovement` secondary timeline for the reingreso loop.
- Seed three new statuses and wire them into the standard `WorkflowDefinition`.
- Return movements from `buildTrace` without breaking existing nodeStatuses.
- Render reingreso count on the inscripción node when > 0.

**Non-Goals:**
- Writing movements (already #832) or validating/writing history (#833).
- Flattening reingreso into exclusive workflow nodes (strategy a).
- New DB column for "returned observed".

## Decisions

- **Strategy (b) over (a).** Issue AC requires picking one. Unbounded reingreso
  is an event stream (`TestimonyMovement` rows). Flattening to N nodes needs an
  arbitrary cap and still cannot show "reingresó 3 veces" as history. Secondary
  timeline preserves the mutually exclusive node model and matches #832's
  deliberate movement modeling.
- **Also seed three linear post-firma statuses** (Generado, Ingresado a
  Inscripción, Retirado). Strategy (b) alone without these leaves Firmada →
  Inscripta; the issue's "reflect the chosen strategy without regressing
  today's 1:1 EstadoDeGestion nodes" is satisfied by extending the catalog for
  the linear part of the puml while keeping the loop off-graph.
- **`returnedObserved` derived as `dateExit != null && !registered`.** No new
  column (single source of truth on dates/flag).
- **Replace Firmada→Inscripta transition/node wiring in a new seed migration**
  rather than leaving an orphan FINAL node for status 10. Leave
  `management_statuses` id 10 row in place (additive Flyway data).
- **New nodes: INTERMEDIATE, INTERMEDIATE, FINAL** (Generado → Ingresado →
  Retirado FINAL).
- **DTO nested entry** `DtoTestimonyMovementEntry` on
  `DtoManagementWorkflowTrace` (mirror `DtoHistoryEntry`) — do not dump full
  `DtoTestimonyMovement` into the trace response.
- **Cloud branch** `cursor/feat-841-workflow-reingreso-loop-69d3` only.

## Riesgos / Trade-offs

- [Seed changes standard workflow for TiposDeTramite 1–3] → Mitigation: migration
  is additive for statuses; transition replace is scoped to definition id 1;
  integration tests cover validation still passes.
- [UI noise if many movements] → Mitigation: timeline collapsed by default or
  capped display with count; design keeps count badge primary signal.
- [Stale draft change confuses implementers] → Mitigation: proposal marks
  `gestion-workflow-reingreso-testimonio` superseded; archive/delete that draft
  in the same implement PR if still present.
- [No movements until users run #832 flows] → Mitigation: unit tests use
  in-memory fixtures; E2E can stub trace JSON.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Generar testimonio advances to Testimonio Generado | unit / integration | `WorkflowTraceServiceTest` + status seed assert |
| Ingresar a inscripción advances status | unit / integration | same |
| Retirar advances to Testimonio Retirado | unit / integration | same |
| Trace includes chronological movements | unit | `WorkflowTraceServiceTest#shouldIncludeAllMovementsInChronologicalOrder` |
| Trace without testimony has empty/absent movements | unit | `WorkflowTraceServiceTest#shouldOmitMovementsWhenNoTestimony` |
| Reingreso appends movement | unit | `WorkflowTraceServiceTest#shouldIncludeAllMovementsInChronologicalOrder` |
| Tracker shows reingreso count | E2E | `frontend/tests/e2e/workflow-tracker.spec.ts` |
| No indicator without observations | E2E | same |
| Workflow without post-firma nodes degrades | E2E / unit FE | same + component test if present |

- New unit tests (`src/test/java/.../unit/`): extend `WorkflowTraceServiceTest`
- New integration tests (`src/test/java/.../integration/`):
  `WorkflowTraceApiH2IntegrationTest` for additive JSON field; Flyway/schema
  assert for new statuses if pattern exists
- Coverage impact (JaCoCo ratchet floor; 80% target): new extraction logic must
  stay covered; no floor lowering

## Regression Strategy

- Existing tests affected: `WorkflowTraceServiceTest`,
  `WorkflowTraceApiH2IntegrationTest`, workflow validation tests if seed graph
  shape changes, Playwright `workflow-tracker` if present
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh` (or
  `integration-test/scripts/test.sh` if that path is the active one on tip)
- Legacy paths at risk: none — do not edit `jpa.*` movement controllers

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`: `workflow-tracker.spec.ts`
  (create if missing)
- Golden path covered: gestión with 2 returned-observed movements shows count
  "2" on inscripción node + timeline entries
- Edge / error paths covered: no movements → no badge; definition without
  post-firma nodes → no crash
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: yes (`V41__extend_workflow_post_firma_testimony.sql`;
  tip on main at implement time was `V40`)
- Deployment order / coupling: migration + API + FE together (additive JSON)
- Configuration or `.env` keys to add (add to `.env.example`, never commit secrets): none
- Feature flag: no
- Smoke test after deploy (Gate 5): open dashboard WorkflowHero for a gestión
  with testimonio movements; confirm nodes + reingreso count

## Rollback Strategy

- Revert safe: FE/API yes; statuses already inserted stay (harmless); restoring
  old Firmada→Inscripta needs a forward migration
- Database rollback: forward-fix only (do not edit applied Vn)
- Data written under the new behavior after revert: status rows 11–13 remain
- Blast radius if rollback is delayed: low (additive UI + catalog)

## Migration Plan

1. Confirm tip Flyway version; write seed migration for statuses + workflow graph.
2. TDD: failing unit tests for movements on trace; failing E2E for badge.
3. DTO + `WorkflowTraceService` extraction via
   `DeedManagement → Procedure → Deed → Testimony → TestimonyMovement`.
4. FE timeline + count; Playwright green.
5. Docs (CU83, FRONTEND-WORKFLOW-TRACKER, CHANGELOG); supersede stale draft.
6. PR with `Closes #841`.

## Open Questions

None material. Strategy (b) is fixed by this Gate 1 pack.
