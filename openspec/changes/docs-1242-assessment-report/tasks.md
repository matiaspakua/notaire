> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1242 exists, labeled, linked to CU76
- [x] 1.2 Use Case documentation exists
- [x] 1.3 Acceptance Criteria defined as scenarios (or `skip_specs` justified)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a unless the design says otherwise
- [x] 1.6 Move the Issue to IN PROGRESS (comment with the branch link)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b docs/1242_assessment_report`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh docs-1242-assessment-report`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 n/a - documentation only; existing link and traceability guards cover the new file
- [x] 3.2 Run `python3 -m unittest discover -s scripts/tests` on the branch
- [x] 3.3 Every scenario maps to a test

## 4. Implementación

- [x] 4.1 Run the baseline suites and record results
- [x] 4.2 Write `docs/300-development/ASSESSMENT-2026-10.md`
- [x] 4.3 Open or comment the finding issues
- [x] 4.4 Guards green

## 5. Actualizar tests existentes

- [x] 5.1 Existing affected tests updated without weakening assertions
- [x] 5.2 No dead code or references remain

## 6. Ejecutar regresión

- [x] 6.1 Backend tests — n/a (no Java touched)
- [x] 6.2 Coverage gate — n/a
- [x] 6.3 `bash scripts/preflight.sh` (or the subset the environment allows)
- [x] 6.4 No `@Disabled` tests

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI change

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `docs/300-development/ASSESSMENT-2026-10.md` — new
- [x] 8.2 `docs/300-development/README.md` — link
- [x] 8.3 `CHANGELOG.md` — n/a, not user visible

## 9. Commits atómicos

- [ ] 9.1 One logical change per commit, Conventional Commits
- [ ] 9.2 Only the final commit carries `Closes #1242`; others `Refs #1242`
- [ ] 9.3 No secrets, no commented-out code

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin docs/1242_assessment_report`
- [ ] 10.2 Open PR `[#1242] docs(assessment): publish the October 2026 full-system assessment`
- [ ] 10.3 Wait for all required workflows
- [ ] 10.4 Gate 4 — CI green, review approved, no conflicts

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: CI green on the merge commit
- [ ] 12.2 Rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1242 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive docs-1242-assessment-report`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Failing tests observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #1242` on the last
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
