> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists — CU76
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (entity null-safety; no architectural change)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 853 --add-label "in-progress"`) — attempted; label API denied for integration token (noted)

## 2. Create branch

- [x] 2.1 `git checkout main && git pull origin main` — after queue through #799/#800/#805/#841
- [x] 2.2 `git checkout -b cursor/test-853-gestion-escritura-unit-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-853/` into
      `openspec/changes/test-853-gestion-escritura-unit/` and run
      `bash scripts/validate-sdlc-plan.sh test-853-gestion-escritura-unit`

## 3. Gate 2 — Write tests (TDD, failing first)

- [x] 3.1 Enumerate cases from delta spec (getDto/getDtoNotary/setAtributos/list init/Person getDto)
- [x] 3.2 Write failing unit tests in `DeedManagementEntityTest` (+ `PersonEntityTest` as needed)
- [x] 3.3 n/a integration beyond optional sparse gestión read
- [x] 3.4 Run and **observe fail** — `mvn test -pl backend-api -Dtest=DeedManagementEntityTest`
- [x] 3.5 Confirm every `#### Scenario:` maps to at least one test

## 4. Implementation

- [x] 4.1 Null-guard `DeedManagement.getDto()` when status is null
- [x] 4.2 Null-guard `getDtoNotary()` when notary or identification type is null
- [x] 4.3 Null-guard `setAtributos()` when status is null
- [x] 4.4 Null-guard `Person.getDto()` for identification type and `DeedManagementList`
- [x] 4.5 Expand unit coverage (equals/hashCode/happy getDto already present — keep green)
- [x] 4.6 CHANGELOG engineering note

## 5. Update existing tests

- [x] 5.1 Update any test that expected NPE to expect null-safe results
- [x] 5.2 Keep existing setAtributos / equals asserts
- [x] 5.3 Remove obsolete expectations only with documented reason

## 6. Run regression

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — Checkstyle, SpotBugs as configured
- [ ] 6.4 Bruno — n/a unless a gestión suite fails; still run preflight
- [ ] 6.5 No `@Disabled` without justification

## 7. Run Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a — no UI surface
- [x] 7.3 n/a — no UI surface
- [x] 7.4 Record “n/a — no UI surface (entity unit null-safety only)”

## 8. Gate 3 — Update permanent documentation

- [x] 8.1 Update every permanent document listed in proposal.md
- [x] 8.2 OpenAPI — n/a (no endpoint contract change)
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 Archive — n/a
- [x] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Atomic commits

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #853` (completing commit) or `Refs #853`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request and CI validation

- [ ] 10.1 `git push -u origin cursor/test-853-gestion-escritura-unit-69d3`
- [ ] 10.2 Open the PR titled `[#853] test(backend): DeedManagement null-safe DTO mapping + unit coverage`, referencing Issue and CU76 — via ManagePullRequest (draft, base main)
- [ ] 10.3 Wait for every required workflow to pass
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main` (coordinator heavy-CI gate)
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test and close

- [ ] 12.1 Confirm `DeedManagementEntityTest` green on main CI after merge
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive test-853-gestion-escritura-unit`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for UI changes (n/a — no UI)
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
