> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU15` / `CU16` / `CU76`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (test infra)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`) — blocked: gh write denied; recorded in traceability Exceptions

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` (fetch origin/main)
- [x] 2.2 `git checkout -b cursor/fix-916-flaky-full-suite-isolation-69d3`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: isolation after seed drain; BusinessWorkflow/Remaining fixture; SimpleControllers split; #848 still rejects
- [ ] 3.2 Write the unit tests for every scenario in the delta spec (SimpleControllers split as dedicated happy/error methods)
- [ ] 3.3 Write the integration tests where applicable (`PaymentFixtureIsolationIntegrationTest` + fixture updates)
- [ ] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=PaymentFixtureIsolationIntegrationTest`
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [ ] 4.1 Add shared test helper for creating person + presupuesto via MockMvc (mirror ManagementArchive / BudgetResumen)
- [ ] 4.2 Update `BusinessWorkflowIntegrationTest$PaymentsWorkflow` to pay against a fixture budget; update related GET-by-budget assertions to use that id where they assert the created payment
- [ ] 4.3 Update `RemainingControllersIntegrationTest$PaymentTests#shouldCreatePayment` to pay against a fixture budget (GETs of seed id 1 may remain read-only)
- [ ] 4.4 Split `SimpleControllersTest` `all` / `allPaths` into happy-path vs error-path tests; add `GlobalExceptionHandler` where needed
- [ ] 4.5 Make isolation proof green; keep product overpayment behavior unchanged

## 5. Actualizar tests existentes

- [ ] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [ ] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [ ] 5.3 Remove tests made genuinely obsolete, stating the reason — none expected

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration (prove **5 consecutive** greens)
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs) as feasible
- [ ] 6.4 `bash integration-test/scripts/test.sh` — HTTP/Bruno API suite — n/a if stack down / test-infra-only
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification
- [ ] 6.6 Keep #848 overpayment tests green (`ProcessPaymentServiceTest`, `PaymentUseCaseTest`, related)

## 7. Ejecutar Playwright

- [ ] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy
- [ ] 7.2 `cd frontend && npx playwright test` — all green
- [ ] 7.3 Verify the affected screens at 320px, 768px and 1024px
- [x] 7.4 If the change has no UI surface, record "n/a — no UI surface" here with the reason — backend test isolation only

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [ ] 8.2 Update OpenAPI/Swagger annotations if endpoints changed, and verify in Swagger UI — n/a
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes — n/a
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` — none
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #<issue-number>`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin <branch-name>`
- [ ] 10.2 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main` (Do NOT merge in this agent run)
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR — n/a until merge
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md` — pending human merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment (health endpoint + the key flow of this change)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR — after merge
- [ ] 12.4 Archive the change: `openspec archive <change-name>` — after merge

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Failing tests written first (TDD) and observed failing
- [ ] Implementation makes those tests pass
- [ ] Full regression green (unit + integration + E2E where applicable)
- [ ] Coverage at or above JaCoCo ratchet floor
- [ ] Permanent documentation updated; no drift vs this change folder
- [ ] PR opened; CI green; review approved
- [ ] Merged via PR; smoke test passed; Issue closed
