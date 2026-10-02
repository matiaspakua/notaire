> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`) — blocked: token cannot mutate labels; exception recorded in traceability.md

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b cursor/fix-1060-audit-log-mutations-69d3`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: POST/PUT/DELETE → 405; OpenAPI tag consult-only; Bruno negatives
- [x] 3.2 Write unit test `shouldRejectUpdateOfAuditRecords` (PUT → 405); assert OpenAPI tag if covered in unit
- [x] 3.3 Extend `AuditRecordMutationDisabledIntegrationTest` with PUT and DELETE → 405
- [x] 3.4 Run them and **observe them fail** where new assertions are not yet met (tag assertion before tag fix)
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Update `AuditRecordController` `@Tag` description to consult-only (no “administrar”)
- [x] 4.2 Add Bruno negatives: `02-post-denied.yml`, `03-put-denied.yml`, `04-delete-denied.yml`
- [x] 4.3 Update `api-test/COVERAGE.md` audit-records row for mutation-deny cases
- [x] 4.4 Confirm controller stays GET-only (no POST/PUT/DELETE handlers)

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected: `AuditRecordControllerTest`, `AuditRecordMutationDisabledIntegrationTest`
- [x] 5.2 Update them without weakening assertions; keep existing POST/DELETE 405 expectations
- [x] 5.3 Remove tests made genuinely obsolete — none expected

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 Bruno suite when stack available — or document deferred to CI `testing/scripts/test.sh`
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy
- [ ] 7.2 `cd frontend && npx playwright test` — all green
- [ ] 7.3 Verify the affected screens at 320px, 768px and 1024px
- [x] 7.4 n/a — no UI surface (audit mutation deny is API-only; frontend already GET-only)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update threat model SR-07 note that HTTP forge is closed (#1060)
- [x] 8.2 Update OpenAPI `@Tag` and verify no create operation in contract
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for #1060 residual closure
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — none
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #1060`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-1060-audit-log-mutations-69d3`
- [ ] 10.2 Open the PR titled `[#1060] security(audit): deny audit-log HTTP mutations`, referencing Issue and Use Case; body includes `Closes #1060`
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: GET audit-log → 200; POST/PUT/DELETE → 405
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue via PR `Closes #1060`
- [ ] 12.4 Archive the change: `openspec archive security-1060-audit-log-mutations-denied`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (n/a — no UI)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
