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

- [ ] 3.1 Enumerate test cases: PUT/complete-case reject; create node validation; same-status PUT OK
- [ ] 3.2 Write unit tests for initial-status / reject-mutation guard if extracted
- [ ] 3.3 Write `ManagementWorkflowStatusWriteEnforcementIntegrationTest`
- [ ] 3.4 Run them and **observe them fail**
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [ ] 4.1 Add shared status-write guard using workflow nodes / transition validator ports
- [ ] 4.2 Reject status id changes on plain PUT and complete-case PUT (400 + require `/transition`)
- [ ] 4.3 Validate initial status on complete-case create (and plain create when workflow resolvable)
- [ ] 4.4 Ensure BusinessValidationException is not swallowed as 500 in controller catch blocks
- [ ] 4.5 Document workflow-trace as legal-next source in OpenAPI; keep UI filtered
- [ ] 4.6 Preserve bitácora on create and `/transition`; English-only in touched code

## 5. Actualizar tests existentes

- [ ] 5.1 Update `ManagementHistorialOrphanWriteIntegrationTest` status-change-via-PUT expectations
- [ ] 5.2 Update `ManagementControllerIntegrationTest` / Bruno if they mutate status via PUT
- [ ] 5.3 Remove obsolete tests only if genuinely replaced by reject scenarios

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 Bruno/HTTP suite — update if PUT status assertions fail
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Confirm TS-0011 / TS-0029 still match filtered destinations + `/transition`
- [ ] 7.2 Run focused Playwright when stack available; otherwise rely on CI
- [ ] 7.3 Viewports already covered by TS-0029
- [ ] 7.4 No new UI surface expected — confirmation only

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update CU02 / CU53 / CU83 (and endpoint registry) per proposal Documentation Impact
- [ ] 8.2 Update OpenAPI summaries on touched endpoints
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) **BREAKING** entry
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` — n/a unless needed
- [ ] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash scripts/preflight.sh --fix` as capacity allows

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #804`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/feat-804-enforce-workflow-transitions-69d3`
- [ ] 10.2 Open draft PR `[#804] feat(api): enforce workflow transitions on gestion status writes` with `Closes #804`
- [ ] 10.3 Wait for required workflows — coordinator watches heavy CI
- [ ] 10.4 Gate 4 — CI green, code review, no conflicts — coordinator merges
- [ ] 10.5 Record the PR number in `traceability.md`

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
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (confirm TS-0011/TS-0029; CI)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
