> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

\#833 shipped `POST /gestiones/{id}/transition` (CU83 use case + validator) and
UI filtering via `workflow-trace`. #806 wired History on orphan create/update
paths. Bypass remains: `applyManagementRequest` and complete-case
`applyManagementFields` still set any status id on update. Archive already goes
through `ManagementTransitionService`.

## Goals / Non-Goals

**Goals:**

- Reject status id changes on plain PUT and complete-case PUT (require `/transition`).
- Validate initial create status against workflow nodes when a workflow exists.
- Reuse shared `WorkflowTransitionValidatorPort` / workflow lookup — no duplicated graph logic.
- Document workflow-trace as the legal-next contract; keep UI filtered.
- Preserve #806 bitácora behavior on create and on successful transition/archive.
- English-only identifiers/comments in touched code.

**Non-Goals:**

- New workflow builder features.
- Renaming Spanish URL segments without ADR.
- Auto-applying transitions from PUT body (reject is clearer).
- Changing archive debt / carpetas-en-espera rules.

## Decisions

1. **Reject + require `/transition` for post-create status mutations** — clearer
   API than silently validating edges on PUT. Same-status PUT still allowed.
2. **Create-time: node membership, prefer INITIAL** — no prior edge required;
   when `ProcedureType.workflowDefinition` exists, status must match a node;
   prefer documenting/enforcing INITIAL when present (reject non-node always;
   if INITIAL nodes exist, prefer requiring one of them for create — if multiple
   INITIAL, any INITIAL; if no INITIAL typed, any node).
3. **Shared validator service** — extract or add
   `ManagementStatusWriteGuard` (or methods on existing transition stack) that
   uses `WorkflowNodeRepository` / `WorkflowTransitionValidatorPort` so
   controller does not reimplement graph rules.
4. **Legal-next = workflow-trace** — no new endpoint unless OpenAPI clarity
   needs `GET /{id}/valid-destinations`; UI already computes from transitions.
5. **Exception handling** — throw `BusinessValidationException` and let it
   propagate (do not swallow into 500 in controller catch-all).
6. **Bitácora** — keep `registerStatus` on create and on `/transition`; remove
   History-on-PUT-status-change expectations because PUT status change is
   rejected (update #806 tests accordingly without stripping create coverage).

## Riesgos / Trade-offs

- [BREAKING for API clients that PUT status] → Document in CHANGELOG/OpenAPI;
  frontend already uses `/transition`.
- [#806 update-status History tests become reject tests] → Update those tests;
  History still written via `/transition`.
- [Plain create without procedure has no workflow] → Allow any defined status;
  complete-case always has tipo → enforce nodes.
- [Double validation if someone later applies transitions on PUT] → Keep single
  guard + `/transition` use case.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Transición válida se aplica | integration | existing `ManagementTransitionControllerIntegrationTest` |
| Transición inválida es rechazada | integration | existing |
| Gestión sin workflow rechaza transición | integration | existing |
| Plain PUT that changes status is rejected | integration | `ManagementWorkflowStatusWriteEnforcementIntegrationTest` (new) |
| Complete-case PUT that changes status is rejected | integration | same |
| Plain PUT that keeps the same status succeeds | integration | same |
| Complete-case create with start-node status succeeds | integration | same |
| Complete-case create with status outside workflow rejected | integration | same |
| Workflow-trace lists transitions for legal next | integration | existing `WorkflowTraceApiH2IntegrationTest` + assert outbound edges |
| UI only offers valid destinations | E2E | TS-0029 / TS-0011 |

- New unit tests: guard/helper for initial-node validation if extracted.
- New integration tests: `ManagementWorkflowStatusWriteEnforcementIntegrationTest`
- Coverage impact: controller/guard branches; ratchet floor held.

## Regression Strategy

- Existing tests affected:
  - `ManagementHistorialOrphanWriteIntegrationTest` — status-change-via-PUT
    cases must expect 400 (or switch to `/transition` for History assertions).
  - `ManagementControllerIntegrationTest` — complete-case update that changes
    status may need adjustment.
  - Bruno `managements/06-update.yml` if it changes status.
- Full suite: `mvn verify -pl backend-api`
- Do not weaken `/transition` rejection assertions.

## Playwright Strategy

- Specs: `TS-0011-gestiones-crud-workflow.spec.ts`,
  `TS-0029-gestion-estado-transition-feature.spec.ts` — confirm filtered
  destinations + `/transition` path; no unfiltered Select of all statuses.
- Golden path: valid destination updates status.
- Edge: initial status not offered as destination when not legal.
- Viewports: already in TS-0029.
- Command: `cd frontend && npx playwright test TS-0011 TS-0029`
- If stack unavailable: record confirmation; CI runs Playwright.

## Deployment Strategy

- Flyway migration required: no
- Deployment order: code-only
- Configuration / `.env`: none
- Feature flag: no
- Smoke (Gate 5): create complete-case with start status → OK; PUT status change
  → 400; POST `/transition` legal → 200; GET workflow-trace shows outbound edges

## Rollback Strategy

- Revert the deploy / PR. No data migration. Clients that already moved to
  `/transition` remain compatible; old PUT-status clients would work again only
  after revert (undesirable long-term).
