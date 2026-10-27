> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1–1.6 Issue #1949, RNF-09, AC, impact, claimed (in-repo)

## 2. Crear branch

- [x] 2.1–2.3 `feat/1949_workflow-diagram-polish` from updated main (no external worktree)

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1–3.5 Extend viewer / hero / node-box tests; observe fail

## 4. Implementación

- [ ] 4.1 WorkflowViewer + WorkflowHero async chrome
- [ ] 4.2 WorkflowNodeBox badge case

## 5. Actualizar tests existentes

- [ ] 5.1–5.3 Keep workflow suite green

## 6. Ejecutar regresión

- [ ] 6.5 frontend vitest for workflow + hero

## 7. Ejecutar Playwright

- [ ] 7.1–7.4 path-scoped CI

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1–8.6 design system + CHANGELOG

## 9. Commits atómicos

- [ ] 9.1–9.4 Closes #1949 on PR

## 10. Pull Request y validación CI

- [ ] 10.1 Archive + push + PR from this root

## 11. Deploy

- [ ] 11.1 Merge via PR

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1–12.2 Smoke dashboard workflow hero

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
