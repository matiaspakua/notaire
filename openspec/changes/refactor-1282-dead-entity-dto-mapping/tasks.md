> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1282 exists, labeled, linked to CU76
- [x] 1.2 Use Case documentation exists
- [x] 1.3 Acceptance Criteria defined as scenarios
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a, follows existing architecture
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b refactor/1282_entity_dto_mapping_removal`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh refactor-1282-dead-entity-dto-mapping`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Add the failing tests; observed failing
- [x] 3.3 Every scenario maps to a test

## 4. Implementación

- [ ] 4.1 Delete the 27 compiler-proven dead mapping methods and their leftover annotations
- [ ] 4.2 Delete the tests that only exercised them
- [ ] 4.3 Add the ratchet baseline
- [ ] 4.4 Tests green

## 5. Actualizar tests existentes

- [ ] 5.1 Existing affected tests updated without weakening assertions
- [ ] 5.2 No dead code or unused imports remain

## 6. Ejecutar regresión

- [ ] 6.1 Targeted tests for the change
- [ ] 6.2 Coverage gate — `mvn verify -pl backend-api` keeps the ratchet floor
- [ ] 6.3 `bash scripts/preflight.sh`
- [ ] 6.4 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 No UI change: the suite runs as regression evidence (`bash scripts/run_pipeline.sh`)

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 `CHANGELOG.md`

## 9. Commits atómicos

- [ ] 9.1 One logical change per commit, Conventional Commits
- [ ] 9.2 Only the final commit carries `Closes #1282`; others `Refs #1282`
- [ ] 9.3 No secrets, no commented-out code

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` exits 0
- [ ] 10.2 `git push -u origin refactor/1282_entity_dto_mapping_removal`
- [ ] 10.3 Open PR `[#1282] refactor(api): delete dead entity/DTO mapping methods (slice 2a of #577)`
- [ ] 10.4 Wait for all required workflows
- [ ] 10.5 Gate 4 — CI green, review approved, no conflicts

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: CD green on the merge commit; CD green
- [ ] 12.2 Rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1282 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive refactor-1282-dead-entity-dto-mapping`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Failing tests observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #1282` on the last
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
