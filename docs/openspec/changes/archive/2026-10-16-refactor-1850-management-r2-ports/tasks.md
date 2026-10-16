> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists (`#1850`), linked to Use Case
- [x] 1.2 Use Case documentation exists (architectural refactor under #1693)
- [x] 1.3 Acceptance Criteria defined in the delta spec
- [x] 1.4 Impact Analysis in `proposal.md`
- [x] 1.5 ADR-030 slice log
- [x] 1.6 Move Issue to IN PROGRESS (label may be blocked)

## 2. Crear branch

- [x] 2.1 Fetch `main` (includes `ce9001bc8`)
- [x] 2.2 `cursor/1850-management-r2-a603`
- [x] 2.3 Record branch in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD)

- [x] 3.1 Update transition/controller tests for domain ports and inbound methods
- [x] 3.2 ArchUnit shrink proves R2 drop and R5 clean
- [x] 3.3 Scenario coverage mapped in `traceability.md`

## 4. Implementación

- [x] 4.1 Domain views + convert CU83 ports (no entity on signatures)
- [x] 4.2 Move entity-bound Management* helpers to adapter.out.persistence.management
- [x] 4.3 Extend OpsPort/inbound for case-summary/financial/pending-balance; auto-transition port; shrink baseline; ADR

## 5. Actualizar tests existentes

- [x] 5.1 TransitionManagementUseCaseTest + Management*Service tests + controllers + ExternalEntityDocumentServiceTest

## 6. Ejecutar regresión

- [x] 6.1 Focused unit + HexagonArchitectureTest
- [x] 6.2 Spotless check

## 7. Ejecutar Playwright

- [x] 7.4 n/a — no UI surface

## 8. Gate 3 — Documentación permanente

- [x] 8.1 ADR-030 slice log
- [x] 8.3 CHANGELOG omit (#1766)

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #<issue-number>`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes`
- [ ] 10.2 `git push -u origin <branch-name>`
- [ ] 10.3 Open the PR titled `[#<issue>] <type>(<scope>): <description>`
- [ ] 10.4 Wait for every required workflow to pass
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
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
