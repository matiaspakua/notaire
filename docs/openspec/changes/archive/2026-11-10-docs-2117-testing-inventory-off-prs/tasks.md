> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU76`)
- [x] 1.2 Use Case documentation exists and is accurate — CU76
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (process tooling)
- [x] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh 2117` — **SKIPPED** (live): `addLabelsToLabelable` 403; honest note recorded; Gate 1 continued

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b docs/2117_testing-inventory-off-prs`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: discovery validate; unprefixed fail; generator real counts; no committed count table
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Write the integration tests where applicable — n/a (scripts + docs)
- [x] 3.4 Run them and **observe them fail** — `python3 -m unittest discover -s testing/tests -p 'test_testing_inventory.py'`
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Change `validate-testing-inventory.py` to discovery-only (print counts; no committed table diff)
- [x] 4.2 Change `generate-testing-inventory.py` to write `INVENTORY.generated.md`; keep real disk counts
- [x] 4.3 Replace committed `INVENTORY.md` with a stable stub; gitignore the generated report
- [x] 4.4 Hook generator into github-page `prebuild`
- [x] 4.5 Update README + PROCESS-ARCHITECTURE + one FLEET-LESSONS line + migration note
- [x] 4.6 Make tests green

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected — `test_testing_inventory.py` expectations for committed monolith
- [x] 5.2 Update them without weakening assertions; document why old stale-diff expectation was wrong
- [x] 5.3 Remove tests made genuinely obsolete (`test_stale_inventory_fails_validation`, `test_inventory_markdown_exists` as committed monolith)

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest discover -s testing/tests -p 'test_testing_inventory.py'` — green
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a (no Java change; covered by preflight)
- [x] 6.3 `mvn verify -pl backend-api` — n/a product; preflight `--full` is the gate
- [x] 6.4 Bruno API suite — n/a product surface; preflight `--full`
- [x] 6.5 No `@Disabled` or skipped tests without documented justification

## 7. Ejecutar Playwright

- [x] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy — n/a
- [x] 7.2 `cd testing/e2e && npx playwright test` — n/a — no UI surface
- [x] 7.3 Verify the affected screens — n/a
- [x] 7.4 If the change has no UI surface, record "n/a — no UI surface" here with the reason — docs/scripts only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger — n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 Archive superseded documents — none
- [x] 8.5 Confirm no information was duplicated
- [x] 8.6 `bash workspace/sdlc/preflight.sh --full` — required before PR

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #<issue-number>` or `Refs #2117` as appropriate
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive docs-2117-testing-inventory-off-prs --yes`
- [ ] 10.2 `git push -u origin docs/2117_testing-inventory-off-prs`
- [ ] 10.3 Open the PR titled `[#2117] …`, referencing Issue and Use Case (`Closes #2117`)
- [ ] 10.4 Wait for every required workflow to pass (or #2001 quiet PR + local `--full`)
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main` (Worker never merges)
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR — n/a docs/scripts
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test — `validate-testing-inventory.py` + generate real counts
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E — via preflight `--full`
- [x] Coverage at or above the JaCoCo ratchet floor — n/a Java
- [x] Playwright E2E green for UI changes — n/a
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
