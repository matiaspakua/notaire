> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — CU22 already documents RF-115 effect; will clarify plain paths at Gate 3
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (reuse existing service)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`) — ACL denied for bot

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b cursor/feat-805-suplencia-assignment-69d3 origin/main`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: plain POST redirect; plain PUT redirect; notes; complete-case regression
- [ ] 3.2 Update `ManagementSubstitutionServiceTest` for English renames (behavior unchanged)
- [ ] 3.3 Add `shouldRedirectNotaryOnPlainCreateWhenActiveSubstitution` and `shouldRedirectNotaryOnPlainUpdateWhenActiveSubstitution` on `ManagementControllerIntegrationTest`
- [ ] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=ManagementControllerIntegrationTest`
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [ ] 4.1 Englishize `ManagementSubstitutionService`: `AssignedNotary`, `resolveNotary`, `redirectionNote`, `appliedSubstitution`; English javadoc + note string
- [ ] 4.2 In `applyManagementRequest`, after `dateStart` is set and notary resolved from repo, call `resolveNotary` and set effective notary
- [ ] 4.3 Append redirection notes via shared `buildNotes` (or equivalent) on plain path
- [ ] 4.4 Update OpenAPI `@Operation` on plain POST/PUT to mention CU22/CU59 substitution
- [ ] 4.5 Update all call sites (`ManagementController`, unit tests) for English names

## 5. Actualizar tests existentes

- [ ] 5.1 Identify existing tests affected (`ManagementSubstitutionServiceTest`, complete-case redirect IT, `AdditionalControllersTest`)
- [ ] 5.2 Update them without weakening assertions — rename-only for unit test API
- [ ] 5.3 Remove tests made genuinely obsolete, stating the reason — none expected

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 Bruno/HTTP suite — n/a unless gestiones create pins notary under active substitution
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 No new E2E specs — no UI surface for plain POST/PUT
- [ ] 7.2 Optional: `npx playwright test TS-0092-gestion-suplencia-redirect.spec.ts` when stack available; else CI
- [ ] 7.3 Viewports: n/a — no UI surface
- [ ] 7.4 Record "n/a — no UI surface" (API residual after #836)

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update CU22 — plain POST/PUT also redirect under active substitution
- [ ] 8.2 Update OpenAPI summaries on touched plain create/update endpoints
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) Fixed entry
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` — n/a
- [ ] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash scripts/preflight.sh --fix` — as capacity allows

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #805`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/feat-805-suplencia-assignment-69d3`
- [ ] 10.2 Open draft PR `[#805] feat(api): consult substitution on plain management notary assignment` with `Closes #805`
- [ ] 10.3 Wait for every required workflow — coordinator runs `bash scripts/check-heavy-ci.sh <pr>`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main` (coordinator merges)
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment (health + plain POST redirect)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive feat-805-suplencia-assignment`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (n/a — no UI surface; TS-0092 regression)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
