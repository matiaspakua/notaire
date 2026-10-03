> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists — CU83, CU06, CU07, CU11, CU44
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (extends ADR-014; strategy documented in design.md)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 841 --add-label "in-progress"`) — defer until implement

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — after queue through #799/#800/#805
- [x] 2.2 `git checkout -b cursor/feat-841-workflow-reingreso-loop-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-841/` into
      `openspec/changes/feat-841-workflow-reingreso-loop/` and run
      `bash scripts/validate-sdlc-plan.sh feat-841-workflow-reingreso-loop`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate cases: new statuses/nodes; movements on trace; reingreso count UI; degrade without post-firma nodes
- [x] 3.2 Write failing unit tests on `WorkflowTraceServiceTest` for movement extraction / ordering / returnedObserved
- [x] 3.3 Write failing integration assert for additive JSON field; Playwright stubs for badge
- [x] 3.4 Run and **observe fail** — `mvn test -pl backend-api -Dtest=WorkflowTraceServiceTest`
- [x] 3.5 Confirm every `#### Scenario:` maps to at least one test

## 4. Implementación

- [x] 4.1 Add Flyway migration `V41__extend_workflow_post_firma_testimony.sql`
      (tip after V40): insert management_statuses 11–13; replace Firmada→Inscripta
      path with Generado → Ingresado → Retirado on definition id 1
- [x] 4.2 Add `DtoTestimonyMovementEntry` + `testimonyMovements` on `DtoManagementWorkflowTrace`
- [x] 4.3 Extend `WorkflowTraceService.buildTrace` to collect movements via DeedManagement → Procedure → Deed → Testimony → TestimonyMovement
- [x] 4.4 Derive `returnedObserved` as `dateExit != null && !registered`; sort by `dateEntry`
- [x] 4.5 FE types + `WorkflowTracker.tsx` secondary timeline + reingreso badge on inscripción node
- [x] 4.6 Remove or archive superseded draft `gestion-workflow-reingreso-testimonio` if still in tree
- [x] 4.7 Docs + CHANGELOG

## 5. Actualizar tests existentes

- [x] 5.1 Update workflow seed expectations if they hard-code 7 nodes / Firmada→Inscripta
- [x] 5.2 Keep existing nodeStatuses / history assertions green (additive field only)
- [x] 5.3 Remove obsolete expectations only with documented reason

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — Checkstyle, SpotBugs as configured
- [ ] 6.4 Bruno / `bash testing/scripts/test.sh` if workflow suites exist
- [ ] 6.5 No `@Disabled` without justification

## 7. Ejecutar Playwright

- [x] 7.1 Add/update `frontend/tests/e2e/workflow-tracker.spec.ts` per design.md
- [ ] 7.2 `cd frontend && npx playwright test` — all green
- [ ] 7.3 Viewport checks: 320 / 768 / 1024
- [ ] 7.4 Confirm WorkflowHero still renders when movements absent

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md
- [x] 8.2 Note strategy (b) in CU83 + FRONTEND-WORKFLOW-TRACKER; cross-ref #832 docs
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 Archive superseded draft change if applicable
- [x] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #841` (completing commit) or `Refs #841`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/feat-841-workflow-reingreso-loop-69d3`
- [ ] 10.2 Open the PR titled `[#841] feat(workflow): reingreso loop as movement timeline on tracker`, referencing Issue and CU83 — via ManagePullRequest (draft, base main)
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml` as applicable
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main` (coordinator heavy-CI gate)
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: dashboard WorkflowHero for a gestión with movements shows count
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive feat-841-workflow-reingreso-loop`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
