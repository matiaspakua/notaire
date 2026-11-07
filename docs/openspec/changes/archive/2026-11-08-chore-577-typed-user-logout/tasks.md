> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU84`)
- [x] 1.2 Use Case documentation exists and is accurate — `docs/100-business/102-use-cases/CU84 - Login.md`
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a, no new ADR
- [x] 1.6 Move the Issue to IN PROGRESS — #577 already has `status:in-progress`

## 2. Crear branch

- [x] 2.1 Branched from updated `origin/main` (`cc70959c`, includes #2079)
- [x] 2.2 `refactor/577_typed-user-logout`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, missing token, baseline shrink
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Write the integration tests where applicable — existing logout integration tests are PRESERVE
- [x] 3.4 Run them and **observe them fail** — shrink baseline first so `ControllerSignatureArchitectureTest` fails on `UserController#logout`
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Type `UserController#logout` as `ResponseEntity<LogoutResponse>`
- [x] 4.2 CHANGELOG Unreleased; sync the main spec; archive in the PR; `Refs #577`

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [x] 5.2 Update them without weakening assertions; document why any old expectation was wrong — none affected
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason — none obsolete

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration (covered by `mvn verify` in preflight --full)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor (covered by `mvn verify`)
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 `bash integration-test/scripts/test.sh` — HTTP/Bruno API suite (preflight --full Bruno job)
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy — n/a, no UI change
- [x] 7.2 `cd testing/e2e && npx playwright test` — preflight --full runs the existing suite
- [x] 7.3 Verify the affected screens at 320px, 768px and 1024px — n/a, no layout change
- [x] 7.4 n/a — no UI surface; logout HTTP contract is unchanged and CU84 screens stay covered

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger annotations if endpoints changed, and verify in Swagger UI — annotations unchanged; HTTP contract unchanged
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — none superseded
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash workspace/sdlc/preflight.sh --full` — PREFLIGHT PASSED (45 passed)

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Refs #577` (multi-PR tip; do not close #577)
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes`
- [ ] 10.2 `git push -u origin <branch-name>`
- [ ] 10.3 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] 10.4 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment (health endpoint + the key flow of this change)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Keep #577 open (multi-PR tip)
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

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
