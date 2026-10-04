> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #956 exists, labeled, linked to CU84
- [x] 1.2 Use Case documentation exists
- [x] 1.3 Acceptance Criteria defined as scenarios (or `skip_specs` justified)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a unless the design says otherwise
- [x] 1.6 Move the Issue to IN PROGRESS (comment with the branch link)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b docs/956_cu84_template`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh docs-956-cu84-template`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: row shape, duplicate IDs, single Login row, template rows in every CU
- [x] 3.2 Add `scripts/test_business_docs_traceability.py` with its wrapper; observed failing
- [x] 3.3 Every scenario maps to a test

## 4. Implementación

- [x] 4.1 Fix the two CSV rows into one with #1224
- [x] 4.2 Migrate CU84 to the template
- [x] 4.3 Guards green

## 5. Actualizar tests existentes

- [x] 5.1 Existing affected tests updated without weakening assertions
- [x] 5.2 No dead code or references remain

## 6. Ejecutar regresión

- [x] 6.1 Backend tests — n/a (no Java touched)
- [x] 6.2 Coverage gate — n/a
- [ ] 6.3 `bash scripts/preflight.sh` (or the subset the environment allows)
- [ ] 6.4 No `@Disabled` tests

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI change

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `docs/100-business/102-use-cases/CU84 - Login.md` — migrated to the template
- [x] 8.2 `docs/100-business/101-requirements/requerimientos.csv` — single valid Login row
- [x] 8.3 `CHANGELOG.md` — one line

## 9. Commits atómicos

- [x] 9.1 One logical change per commit, Conventional Commits
- [x] 9.2 Only the final commit carries `Closes #956`; others `Refs #956`
- [x] 9.3 No secrets, no commented-out code

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin docs/956_cu84_template`
- [ ] 10.2 Open PR `[#956] docs(business): migrate CU84 to the Use Case template and fix the requirements CSV`
- [ ] 10.3 Wait for all required workflows
- [ ] 10.4 Gate 4 — CI green, review approved, no conflicts

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: CI green on the merge commit
- [ ] 12.2 Rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #956 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive docs-956-cu84-template`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Failing tests observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #956` on the last
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
