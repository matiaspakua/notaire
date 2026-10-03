> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 804 --add-label "in-progress"`) — attempted; GraphQL label ACL denied

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b cursor/feat-804-enforce-workflow-transitions-69d3 origin/main`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: PUT/complete-case reject; create node validation; same-status PUT OK
- [x] 3.2 Write unit tests for initial-status / reject-mutation guard if extracted
- [x] 3.3 Write `ManagementWorkflowStatusWriteEnforcementIntegrationTest`
- [x] 3.4 Run them and **observe them fail** — 5 failures before implementation
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Add shared status-write guard using workflow nodes / transition validator ports
- [x] 4.2 Reject status id changes on plain PUT and complete-case PUT (400 + require `/transition`)
- [x] 4.3 Validate initial status on complete-case create (and plain create when workflow resolvable)
- [x] 4.4 Ensure BusinessValidationException is not swallowed as 500 in controller catch blocks
- [x] 4.5 Document workflow-trace as legal-next source in OpenAPI; keep UI filtered
- [x] 4.6 Preserve bitácora on create and `/transition`; English-only in touched code

## 5. Actualizar tests existentes

- [x] 5.1 Update `ManagementHistorialOrphanWriteIntegrationTest` status-change-via-PUT expectations
- [x] 5.2 Update `ManagementControllerIntegrationTest` / Bruno if they mutate status via PUT — Bruno same-status OK; unit mock ctor updated
- [x] 5.3 Remove obsolete tests only if genuinely replaced by reject scenarios — replaced with reject assertions

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration (via verify; 1970 tests, 0 failures)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor (via verify; all checks met)
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 Bruno/HTTP suite — `06-update.yml` keeps same `managementStatusId`; COVERAGE.md noted
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Confirm TS-0011 / TS-0029 still match filtered destinations + `/transition`
- [ ] 7.2 Run focused Playwright when stack available; otherwise rely on CI
- [x] 7.3 Viewports already covered by TS-0029
- [x] 7.4 No new UI surface expected — confirmation only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update CU02 / CU53 / CU83 (and endpoint registry) per proposal Documentation Impact
- [x] 8.2 Update OpenAPI summaries on touched endpoints
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) **BREAKING** entry
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — n/a
- [x] 8.5 Confirm no information was duplicated
- [x] 8.6 `bash scripts/preflight.sh --fix` — backend/frontend green; repo-wide SDLC noise from closed-issue leftovers

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #804`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin cursor/feat-804-enforce-workflow-transitions-69d3`
- [x] 10.2 Open draft PR `[#804] feat(api): enforce workflow transitions on gestion status writes` with `Closes #804` — #1199
- [ ] 10.3 Wait for required workflows — coordinator watches heavy CI
- [ ] 10.4 Gate 4 — CI green, code review, no conflicts — coordinator merges
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — coordinator; do not merge from this agent
- [ ] 11.2 Confirm the CD pipeline published the image — coordinator
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test on target environment after merge
- [ ] 12.2 Verify rollback path still available
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive feat-804-enforce-workflow-transitions`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression (`mvn verify -pl backend-api`); E2E via CI
- [x] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes — TS-0011/TS-0029 confirmed; CI runs suite
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4) — draft #1199
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
