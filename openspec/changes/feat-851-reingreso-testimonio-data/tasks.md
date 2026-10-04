> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #851 exists, labeled, linked to CU44
- [x] 1.2 Use Case documentation exists
- [x] 1.3 Acceptance Criteria defined as scenarios (or `skip_specs` justified)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a unless the design says otherwise
- [ ] 1.6 Move the Issue to IN PROGRESS (comment with the branch link)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b feat/851_reingreso_testimonio`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh feat-851-reingreso-testimonio-data`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: reenter with data, without a body, observed without notes, UI dialog
- [ ] 3.2 Add the failing unit, integration and hook tests; observed failing
- [ ] 3.3 Every scenario maps to a test

## 4. Implementación

- [ ] 4.1 Add migration V42 and the entity and DTO field
- [ ] 4.2 Extend the service and the controller body
- [ ] 4.3 Add the reentry dialog, hook and i18n
- [ ] 4.4 Tests green

## 5. Actualizar tests existentes

- [ ] 5.1 Existing affected tests updated without weakening assertions
- [ ] 5.2 No dead code or references remain

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api -Dtest=TestimonyMovement*`
- [ ] 6.2 Coverage gate — `mvn verify -pl backend-api` keeps the ratchet floor
- [ ] 6.3 `bash scripts/preflight.sh` (or the subset the environment allows)
- [ ] 6.4 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI change

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 `docs/100-business/102-use-cases/CU44 – Reingresar testimonio.md` — traceability to the implemented data
- [ ] 8.2 `docs/200-architecture/205-data-model/Diccionario de Datos.md` — regenerated with the new column
- [ ] 8.3 `CHANGELOG.md` — one entry

## 9. Commits atómicos

- [ ] 9.1 One logical change per commit, Conventional Commits
- [ ] 9.2 Only the final commit carries `Closes #851`; others `Refs #851`
- [ ] 9.3 No secrets, no commented-out code

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/851_reingreso_testimonio`
- [ ] 10.2 Open PR `[#851] feat(testimony): capture cartón number, observation and notes on reentry (CU44)`
- [ ] 10.3 Wait for all required workflows
- [ ] 10.4 Gate 4 — CI green, review approved, no conflicts

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: CI green on the merge commit; reentering a withdrawn testimony from the UI stores the data
- [ ] 12.2 Rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #851 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive feat-851-reingreso-testimonio-data`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Failing tests observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #851` on the last
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
