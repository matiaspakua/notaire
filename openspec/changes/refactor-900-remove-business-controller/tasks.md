> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #900 exists, labeled, linked to CU76
- [x] 1.2 Use Case documentation exists
- [x] 1.3 Acceptance Criteria defined as scenarios (or `skip_specs` justified)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a unless the design says otherwise
- [ ] 1.6 Move the Issue to IN PROGRESS (comment with the branch link)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b refactor/900_remove_business_controller`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh refactor-900-remove-business-controller`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: exact id, id prefix collision (1 vs 11), unknown, null, exact name, case, unknown name, null name
- [ ] 3.2 Write `IdentificationTypeLookupTest`; observe it fail (class missing)
- [ ] 3.3 Write the static guard `scripts/test_no_business_controller.py`; observe it fail
- [ ] 3.4 Every scenario maps to a test

## 4. Implementación

- [ ] 4.1 Add `IdentificationTypeLookup`
- [ ] 4.2 Switch the four callers
- [ ] 4.3 Delete `BusinessController.java` and the pom excludes
- [ ] 4.4 Fix compile and tests that referenced the removed class
- [ ] 4.5 Update the SAD and CHANGELOG

## 5. Actualizar tests existentes

- [ ] 5.1 Existing affected tests updated without weakening assertions
- [ ] 5.2 No dead code or references remain

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api`
- [ ] 6.2 Coverage gate — `mvn verify -pl backend-api` keeps the ratchet floor
- [ ] 6.3 `bash scripts/preflight.sh` (or the subset the environment allows)
- [ ] 6.4 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI change

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 `docs/200-architecture/201-SAD/sad.md` — debt tables mark the god class removed
- [ ] 8.2 `CHANGELOG.md` — one entry

## 9. Commits atómicos

- [ ] 9.1 One logical change per commit, Conventional Commits
- [ ] 9.2 Only the final commit carries `Closes #900`; others `Refs #900`
- [ ] 9.3 No secrets, no commented-out code

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin refactor/900_remove_business_controller`
- [ ] 10.2 Open PR `[#900] refactor(backend): remove the BusinessController god class`
- [ ] 10.3 Wait for all required workflows
- [ ] 10.4 Gate 4 — CI green, review approved, no conflicts

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: CI green on the merge commit; login and person lookup work in the stack
- [ ] 12.2 Rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #900 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive refactor-900-remove-business-controller`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Failing tests observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #900` on the last
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
