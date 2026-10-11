> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #921 is open and linked to CU76
- [x] 1.2 Use cases CU02, CU06, CU16, and CU83 exist
- [x] 1.3 Acceptance criteria are in the design testing table (`skip_specs: true`)
- [x] 1.4 Impact analysis is in `proposal.md`
- [x] 1.5 No new ADR
- [ ] 1.6 #921 stays open

## 2. Crear branch

- [x] 2.1 Branch `cursor/business-docs-assessment-853b` from updated `main`
- [x] 2.2 Recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Missing claimed document fails
- [x] 3.2 Claimed document present passes
- [x] 3.3 No traceability file passes
- [x] 3.4 Observed exit 2 before `check-doc-traceability.py` existed

## 4. Implementación

- [x] 4.1 Assessment and `GESTION.md`
- [x] 4.2 Activity and create-management diagram corrected
- [x] 4.3 Lifecycle HTTP paths updated to the English routes
- [x] 4.4 Constitution §8, fleet rule, and the Process Checks step

## 5. Actualizar tests existentes

- [x] 5.1 No existing assertion weakened
- [x] 5.2 No obsolete test removed

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest workspace.tests.test_pr_checks.DocTraceabilityTest`
- [x] 6.2 `python3 docs/tests/test_docs_links.py`
- [x] 6.3 `bash workspace/sdlc/validate-sdlc-plan.sh docs-921-business-assessment`
- [x] 6.4 Maven, Bruno, and Playwright — n/a

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Documents listed in `traceability.md` are in the diff
- [x] 8.2 OpenAPI — n/a
- [x] 8.3 `CHANGELOG.md` Unreleased note
- [x] 8.4 Legacy diagrams kept and corrected, not archived
- [x] 8.5 States stay in `CICLO-DE-VIDA.md`

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 Messages reference #921 without `Closes`
- [x] 9.3 No secrets

## 10. Pull Request y validación CI

- [ ] 10.1 Push
- [ ] 10.2 Open the pull request
- [ ] 10.3 Process Checks green
- [ ] 10.4 Review approved

## 11. Deploy

- [ ] 11.1 Merge via the pull request
- [x] 11.2 Image publish — n/a
- [ ] 11.3 Record the merge commit after merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Process Checks on the pull request include the new step
- [x] 12.2 Rollback is a revert
- [ ] 12.3 Do not close #921
- [ ] 12.4 Archive after merge

## Definition of Done

- [x] Issue linked to a Use Case
- [x] Specification written
- [x] Failing test observed first
- [x] Doc-traceability tests green
- [x] JaCoCo — n/a
- [x] Playwright — n/a
- [x] Permanent documentation updated
- [x] Commits reference #921
- [ ] CI green and review approved
- [ ] Merged, #921 still open
- [x] `traceability.md` records the chain through the branch; merge stays pending
