# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1340 exists with acceptance criteria
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/1340_deed_pickers` from updated `main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Vitest `deed-picker.test.tsx` red (module missing); TS-0123 3/3 failing on the pre-change build
- [x] 3.2 Vitest `no-whole-table-loads.test.ts` red (usePersonas, apiGetPaged)

## 4. Implementación

- [x] 4.1 DeedPicker + useDeedSearch, three forms wired, folio exclusion kept
- [x] 4.2 useEscrituras(), usePersonas() and apiGetPaged removed

## 5. Actualizar tests existentes

- [x] 5.1 TS-0012, TS-0031, TS-0082, folios-vinculacion use `setup/deed-picker.ts`; `use-personas-pagination.test.tsx` removed with its hook

## 6. Ejecutar regresión

- [x] 6.1 `bash frontend/verify.sh` green; full chromium suite (see PR)

## 7. Ejecutar Playwright

- [x] 7.1 TS-0123, TS-0012, TS-0031, TS-0082, TS-0090, folios-vinculacion

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; the PR body says `Refs #1340`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Deploy

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test

## Definition of Done

- [ ] All gates passed
