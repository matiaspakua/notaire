> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — CU22 updated for plain paths
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a
- [ ] 1.6 Move the Issue to IN PROGRESS — ACL denied for bot

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b cursor/feat-805-suplencia-assignment-69d3 origin/main`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: plain POST redirect; plain PUT redirect; notes; complete-case regression
- [x] 3.2 Update `ManagementSubstitutionServiceTest` for English renames
- [x] 3.3 Add plain POST/PUT redirect methods on `ManagementControllerIntegrationTest`
- [x] 3.4 Observed fail — 2/2 failures (expected substitute id, got requested)
- [x] 3.5 Every `#### Scenario:` maps to at least one test

## 4. Implementación

- [x] 4.1 Englishize `ManagementSubstitutionService` API + note string
- [x] 4.2 `applyManagementRequest` calls `resolveNotary` after `dateStart`
- [x] 4.3 Append redirection notes via shared `buildNotes`
- [x] 4.4 Update OpenAPI `@Operation` on plain POST/PUT
- [x] 4.5 Update call sites + frontend toast detector

## 5. Actualizar tests existentes

- [x] 5.1 Identified affected tests (unit + complete-case IT)
- [x] 5.2 Updated without weakening assertions
- [x] 5.3 No obsolete tests removed

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` via verify — 1978 tests, 0 failures
- [x] 6.2 `mvn jacoco:check -pl backend-api` via verify
- [x] 6.3 `mvn verify -pl backend-api` BUILD SUCCESS
- [x] 6.4 Bruno/HTTP — n/a (no Bruno asserts notary under active substitution)
- [x] 6.5 No `@Disabled` tests added

## 7. Ejecutar Playwright

- [x] 7.1 No new E2E specs — no UI surface for plain POST/PUT
- [ ] 7.2 TS-0092 regression via CI `playwright-e2e.yml` (toast marker updated)
- [x] 7.3 Viewports: n/a — no UI surface
- [x] 7.4 Recorded "n/a — no UI surface"

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 CU22 — plain POST/PUT redirect documented
- [x] 8.2 OpenAPI summaries on plain create/update
- [x] 8.3 `CHANGELOG.md` Fixed entry
- [x] 8.4 Archive — n/a
- [x] 8.5 No duplication
- [ ] 8.6 `bash scripts/preflight.sh --fix` — coordinator heavy CI

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 Every commit ends with `Closes #805`
- [x] 9.3 No secrets / dead code
- [x] 9.4 SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 Pushed `cursor/feat-805-suplencia-assignment-69d3`
- [x] 10.2 Draft PR #1206
- [ ] 10.3 Coordinator: `bash scripts/check-heavy-ci.sh 1206`
- [ ] 10.4 Gate 4 pending CI
- [x] 10.5 PR recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Coordinator merges after heavy CI green
- [ ] 11.2 Confirm CD published image
- [ ] 11.3 Record merge commit

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: plain POST redirect under active substitution
- [ ] 12.2 Rollback path available (code-only revert)
- [ ] 12.3 Close #805
- [ ] 12.4 `openspec archive feat-805-suplencia-assignment`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression (`mvn verify`)
- [x] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E: n/a new UI; TS-0092 regression via CI
- [x] Permanent documentation updated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
