# Tasks

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #169 exists, labeled, linked to CU16
- [x] 1.2 Use Case documentation exists (`CU16 – Archivar Gestión.md`)
- [x] 1.3 Acceptance Criteria defined as scenarios
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a unless the design says otherwise

## 2. Crear branch

- [x] 2.1 `feat/169_gestion-archive-deuda-check` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `GestionArchiveIntegrationTest` and related coverage observed failing before the change

## 4. Implementación

- [x] 4.1 `ManagementArchiveDebtService`, `GET /outstanding-balance`, and `pendingDebtAtArchiving` persistence
- [x] 4.2 Frontend warning on `managements/page.tsx` before archive confirm

## 5. Actualizar tests existentes

- [x] 5.1 Existing archive tests adapted without weakening assertions

## 6. Ejecutar regresión

- [x] 6.1 Backend suite and archive integration green for the shipped warn-not-block behavior

## 7. Ejecutar Playwright

- [x] 7.1 Covered by existing managements archive flows where applicable

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see proposal / spec; shipped via #826 / #915)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits on the shipping PRs

## 10. Pull Request y validación CI

- [x] 10.1 Shipping PRs merged (`#826`, `#915`)

## 11. Deploy

- [x] 11.1 Deployed with the shipping merges

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Archive the change under `docs/openspec/changes/archive/` when Gate 5 runs
- [ ] 12.2 Close or retarget Issue #169 if no further CU16 slices remain

## Definition of Done

- [x] Implementation tasks in groups 3–8 complete for the shipped warn-not-block slice
