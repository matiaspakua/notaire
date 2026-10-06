> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #1292 exists, labeled, linked to CU76
- [ ] 1.2 Use Case documentation exists
- [ ] 1.3 Acceptance Criteria defined as scenarios
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 ADR — n/a, follows existing architecture
- [ ] 1.6 Move the Issue to IN PROGRESS (`in-progress` label)

## 2. Crear branch

- [ ] 2.1 `git fetch origin main`
- [ ] 2.2 `git checkout -b refactor/1292_phase1_module_separation`
- [ ] 2.3 Branch name recorded in `traceability.md`
- [ ] 2.4 Run `bash scripts/validate-sdlc-plan.sh refactor-1292-module-separation`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [ ] 3.2 Add the failing tests; observed failing
- [ ] 3.3 Every scenario maps to a test

## 4. Implementación

- [ ] 4.1 Slice 1: manifest, MODULE.md and verify.sh per module; later slices per the issue
- [ ] 4.2 Tests green

## 5. Actualizar tests existentes

- [ ] 5.1 Existing affected tests updated without weakening assertions
- [ ] 5.2 No dead code or unused imports remain

## 6. Ejecutar regresión

- [ ] 6.1 Targeted tests for the change
- [ ] 6.2 Coverage gate — `mvn verify -pl backend-api` keeps the ratchet floor
- [ ] 6.3 `bash scripts/preflight.sh`
- [ ] 6.4 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI change; run the existing suite with preflight --full

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 `docs/200-architecture/202-ADR/ADR-026-module-separation.md`
- [ ] 8.2 `CHANGELOG.md`

## 9. Commits atómicos

- [ ] 9.1 One logical change per commit, Conventional Commits
- [ ] 9.2 Only the final commit carries `Closes #1292`; others `Refs #1292`
- [ ] 9.3 No secrets, no commented-out code

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` exits 0
- [ ] 10.2 `git push -u origin refactor/1292_phase1_module_separation`
- [ ] 10.3 Open PR `[#1292] refactor(repo): phase 1 module separation`
- [ ] 10.4 Wait for all required workflows
- [ ] 10.5 Gate 4 — CI green, review approved, no conflicts

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: CD green on the merge commit; CD green on each slice merge
- [ ] 12.2 Rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1292 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive refactor-1292-module-separation`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Failing tests observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #1292` on the last
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
