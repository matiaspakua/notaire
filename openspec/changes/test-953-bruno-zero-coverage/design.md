> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #953 (TEST, CASO-DE-USO, priority:medium, audit-2026-09). CU76.
Researched on `origin/main` tip `6b246a72`. Suite lives at
`backend-api/api-test/` (YAML OpenCollection, not `.bru`). COVERAGE.md lists
16 TODO controllers. English adapters and paths verified:

| Issue name | Controller | Base path | Ops note |
|------------|------------|-----------|----------|
| CarpetaTramite | ProcedureFolderController | `/api/v1/carpetas` | GET list/id; PUT espera (no full CRUD) |
| Copia | CopyController | `/api/v1/copia` | full CRUD |
| Cuaderno | NotebookController | `/api/v1/cuadernos` | GET/POST + PDF carátula (no PUT/DELETE) |
| DocumentoPresentado | SubmittedDocumentController | `/api/v1/documento-presentado` | full CRUD |
| Gestion | ManagementController | `/api/v1/gestiones` | large surface — cover core CRUD + 1–2 key actions; history folder already covers some management fixtures |
| MinutaInscripcion | RegistrationDraftController | `/api/v1/minutas-inscripcion` | get/create + presentar/observar/inscribir |
| MovimientoTestimonio | TestimonyMovementController | `/api/v1/movimiento-testimonio` | CRUD + domain posts |
| PlantillaCostoDocumento | DocumentCostTemplateController | `/api/v1/plantilla-costos-documento` | POST + GET by tipo-tramite |
| ProtocoloAuxiliar | AuxiliaryProtocolController | `/api/v1/protocolo-auxiliar` | folios-disponibles + escrituras |
| Reporte | ReportController | `/api/v1/reportes` | GET PDF endpoints (sample subset with fixtures) |
| Rol | RoleController | `/api/v1/roles` | CRUD + user role assign/remove |
| Testimonio | TestimonyController | `/api/v1/testimonio` | CRUD + generar/verificar |
| WorkflowDefinition | WorkflowDefinitionController | `/api/v1/workflow-definition` | full CRUD |
| WorkflowNode | WorkflowNodeController | `/api/v1/workflow-node` | CRUD by-workflow |
| WorkflowTransition | WorkflowTransitionController | `/api/v1/workflow-transition` | CRUD by-workflow |
| WorkflowValidation | WorkflowValidationController | `/api/v1/workflow-definition/{id}/validate` | POST validate |

Queue ahead: `#1040 → #1043 → #1045 → #1056 → #1055 → #1050 → #1058 → #976`
(then #945 before or after this per coordinator — Bruno-heavy, low Playwright
product risk).

## Goals / Non-Goals

**Goals:**

- Zero remaining “TODO — resources not yet covered” entries for the 16 names.
- `bru run . -r --env Development` green twice in a row (idempotent).
- Docs/matrix updated.

**Non-Goals:**

- 100% of every ReportController PDF variant in one PR if fixtures are heavy —
  minimum: representative report endpoints with assertions + matrix honesty for
  any deferred report rows (still prefer covering as many as practical).
- Rewriting already-covered folders.
- DAST/OpenAPI (#1067).

## Decisions

1. **Phase implementation tasks by dependency clusters** (one PR still OK if CI
   allows; split PRs only if suite runtime explodes — prefer one Issue/one
   change):
   - A: Rol, WorkflowDefinition/Node/Transition/Validation
   - B: Copia, DocumentoPresentado, Testimonio, MovimientoTestimonio
   - C: Cuaderno, ProtocoloAuxiliar, CarpetaTramite, PlantillaCostoDocumento
   - D: MinutaInscripcion, Gestion (core), Reporte (representative PDFs)

2. **Folder naming**: English kebab folders aligned with suite
   (`roles`, `copies`, `notebooks`, `workflow-definitions`, …).

3. **Coverage depth**: Match issue AC — “full CRUD lifecycle (or the operations
   it exposes)”. Do not invent DELETE for controllers without DELETE.

4. **Defects found**: Trivial green-blocker fixes may land here with clear
   commit notes; larger defects → new Issues, document in COVERAGE.md.

5. **Gestion overlap**: `history/` already creates management fixtures —
   reuse vars/`00-auth` patterns; avoid duplicate leaking rows.

## Riesgos / Trade-offs

- [Heavy suite runtime] → Phase folders; keep assertions focused; share fixtures.
- [PDF/binary reports] → Assert status + content-type; avoid brittle byte equals.
- [Workflow graph complexity] → Create definition → nodes → transitions →
  validate in order; teardown reverse order.
- [Auth/roles side effects] → Prefer disposable users; restore role links.
- [False green bare calls] → Every request file needs chai `test(...)` blocks.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Each of 16 controllers has a Bruno folder | API (Bruno) | `backend-api/api-test/<folder>/` |
| Assertions on status/body | API (Bruno) | chai in each YAML |
| Idempotent double run | API (Bruno) | `bru run . -r --env Development` ×2 |
| Docs list no longer TODOs those 16 | docs/static | COVERAGE.md / matrix review |
| CI Bruno job consumes new folders | CI | existing workflow |

- New unit tests (`src/test/java/.../unit/`): n/a unless a tiny product fix
- New integration tests: Bruno is the contract layer
- Coverage impact (JaCoCo): none expected for tests-only

TDD: add a failing folder (or failing assertion) for the first controller,
observe fail against missing coverage / wrong path, then implement requests
until green; repeat per cluster.

## Regression Strategy

- Existing tests affected: any shared Bruno env vars; `00-auth`; fixture
  collisions with `history`/`people`/`deeds`.
- Full suite: `cd backend-api/api-test && bru run . -r --env Development`
- `mvn verify -pl backend-api` sanity if Java touched
- Legacy paths: do not call removed Swing clients

## Playwright Strategy

- n/a — no UI surface (API contract tests only).
- PR must still pass required CI including Playwright if triggered; no product
  E2E edits expected.

## Deployment Strategy

- Flyway migration required: no
- Deployment order: merge after Bruno CI green; no runtime deploy behavior
- Configuration / `.env`: none new
- Feature flag: no
- Smoke: Bruno suite green on CI and locally against compose stack

## Rollback Strategy

- Revert safe: yes (delete new folders / revert docs)
- Database rollback: none (tests should not leave rows; if they did, cleanup
  scripts / manual delete)
- Blast radius: temporary CI red if partial merge — prefer atomic green suite

## Migration Plan

1. Cluster A → green locally
2. Clusters B–D → green
3. Docs/matrix/CHANGELOG
4. Double `bru run` proof
5. PR

## Open Questions

None material. Representative ReportController coverage is acceptable if every
PDF endpoint cannot be fixture-fed in one change — then matrix must mark any
remaining report rows honestly and open a tiny follow-up rather than claiming
full report coverage falsely.
