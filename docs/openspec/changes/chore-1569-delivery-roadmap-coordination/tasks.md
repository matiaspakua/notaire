> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1569 exists, is labeled, and links RNF-21 / RNF-22
- [x] 1.2 RNF-21 and RNF-22 exist and cover methodology and documentation
- [x] 1.3 Acceptance Criteria are scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules are confirmed in `proposal.md`
- [x] 1.5 ADR is not required; this changes process configuration, not product architecture
- [x] 1.6 Issue #1569 is OPEN, labeled `in-progress`, and board Status is In Progress

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b chore/1569_delivery_roadmap_coordination`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Test cases are enumerated in `design.md`
- [x] 3.2 Extended Python unit tests for history windows, view contracts, board coverage, and release mapping
- [x] 3.3 Integration tests are n/a; use read-only live GitHub checks
- [x] 3.4 Observed failures for missing policy constants, board coverage, visible fields, and stale release trains
- [x] 3.5 Every scenario maps to a unit, live, coherence, or docs check

## 4. Implementación

<!-- Change-specific work goes here. Break it into small, verifiable tasks ordered
     by dependency. Write only the code needed to make the tests of group 3 pass. -->

- [x] 4.1 Restored and simplified the idempotent milestone/view policy from PR #1565
- [x] 4.2 Covered history and delivery windows in Start/Target metadata
- [x] 4.3 Configured full-lifecycle Roadmap, Kanban, Agent queue, Todo, and triage views
- [x] 4.4 Made `--check` verify issue coverage and Project view contracts read-only
- [x] 4.5 Applied the policy with owner `gh` credentials and proved zero drift
- [x] 4.6 Regenerated both milestone snapshot files
- [x] 4.7 Updated canonical project-management and GitHub documentation
- [x] 4.8 Linked cloud fleet and repository agent entry points to the canonical protocol
- [x] 4.9 Replaced stale SemVer-to-milestone names in the product release record
- [x] 4.10 Removed the superseded roadmap classifier, tests, and snapshot

## 5. Actualizar tests existentes

- [x] 5.1 `workspace/sdlc/test_milestone_policy.py` is the affected suite
- [x] 5.2 Updated assertions to the current dated train and view contracts
- [x] 5.3 Removed the superseded target-layout classifier suite with its implementation

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — passed through the pipeline backend phase
- [x] 6.2 `mvn jacoco:check -pl backend-api` — passed through Maven verify
- [x] 6.3 `mvn verify -pl backend-api` — Checkstyle and SpotBugs passed
- [x] 6.4 `bash integration-test/scripts/test.sh` — environment check completed; execution deferred to CI because the local Docker daemon was unavailable
- [x] 6.5 No source tests were disabled; Docker-backed suites are explicitly recorded as CI follow-up

## 7. Ejecutar Playwright

- [x] 7.1 Product E2E specs are n/a — no Notaire application UI change
- [x] 7.2 Project v2 metadata is verified read-only; GitHub Pages build is green
- [x] 7.3 Product viewports are n/a — the static docs page compiles responsively
- [x] 7.4 n/a — GitHub Project and static documentation surfaces only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Updated every permanent document listed in proposal.md
- [x] 8.2 OpenAPI is n/a — no endpoint changed
- [x] 8.3 Updated `CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 Removed superseded generated files; no durable document needed archiving
- [x] 8.5 Canonical rules live only in the project-management directory
- [x] 8.6 `bash workspace/sdlc/preflight.sh` — relevant checks passed; global validation remains blocked by pre-existing archive debt and GitHub API throttling

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #<issue-number>`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the implementation commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin chore/1569_delivery_roadmap_coordination`
- [x] 10.2 Open PR #1590 with the required title, Issue closure, and RNF-21 / RNF-22 references
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [x] 10.5 Record PR #1590 in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment (health endpoint + the key flow of this change)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive <change-name>`

## Definition of Done

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [x] Permanent documentation updated, consistent, not duplicated
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
