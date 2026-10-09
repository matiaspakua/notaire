# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1315 exists with acceptance criteria (empty, justified list)
- [x] 1.2 Specification written in this change

## 2. Crear branch

- [x] 2.1 `fix/1315_accepted_breaking_changes_dir` from #1382's head (same workflow steps)

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `test_check_accepted_breaking_changes.py` (21 tests) observed failing 19 of 21; `test_dast_contract_backup_assets.py` 3 of 12

## 4. Implementación

- [x] 4.1 Checker, directory + README, workflow, preflight

## 5. Actualizar tests existentes

- [x] 5.1 DAST/contract guard reads the directory; assertions kept

## 6. Ejecutar regresión

- [x] 6.1 workspace, contracts, docs and security verify green; real oasdiff: #1374's 7 entries as one file pass and the assembled list makes the diff clean (without it: breaking); run_pipeline.sh not run (no Docker)

## 7. Ejecutar Playwright

- [x] 7.1 Not applicable (no UI change)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Documentation updated (see traceability)

## 9. Commits atómicos

- [x] 9.1 test commit first, then the implementation, `Refs #1315`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 CI green

## 11. Deploy

- [ ] 11.1 Not applicable (CI only)

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 First pull request after the merge accepts a break with its own file

## Definition of Done

- [ ] All gates passed
