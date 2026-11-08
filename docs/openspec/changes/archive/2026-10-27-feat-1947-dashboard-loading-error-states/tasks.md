> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1947 + RNF-09
- [x] 1.2 Use Case exists
- [x] 1.3 AC in delta spec
- [x] 1.4 Impact Analysis
- [x] 1.5 No ADR
- [x] 1.6 Claimed

## 2. Crear branch

- [x] 2.1–2.3 `feat/1947_dashboard-loading-error-states`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1–3.5 `async-states.test.tsx` observed failing

## 4. Implementación

- [ ] 4.1 LoadingState + ErrorState
- [ ] 4.2 DataTable error branch
- [ ] 4.3 Dashboard / people / managements pilots + i18n retry

## 5. Actualizar tests existentes

- [ ] 5.1–5.3 Keep page tests green

## 6. Ejecutar regresión

- [ ] 6.1–6.4 n/a backend
- [ ] 6.5 frontend vitest / tsc / eslint

## 7. Ejecutar Playwright

- [ ] 7.1–7.4 path-scoped CI; preserve testids

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1–8.6 design system + CHANGELOG

## 9. Commits atómicos

- [ ] 9.1–9.4 Conventional Commits; Closes #1947 on PR

## 10. Pull Request y validación CI

- [ ] 10.1 Archive OpenSpec
- [ ] 10.2–10.5 Push + PR + CI

## 11. Deploy

- [ ] 11.1 Merge via PR

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1–12.2 Smoke + close

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
