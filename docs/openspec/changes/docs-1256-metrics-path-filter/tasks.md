> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). Groups 1-12 mandatory.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1256 + CU76
- [x] 1.2 Use Case exists
- [x] 1.3 AC (`skip_specs`)
- [x] 1.4 Impact Analysis
- [x] 1.5 n/a ADR
- [ ] 1.6 Label — 403 likely

## 2. Crear branch

- [x] 2.1 Fetch main
- [x] 2.2 Branch created
- [x] 2.3 Recorded
- [x] 2.4 validate-sdlc-plan

## 3. Gate 2 — Tests first

- [x] 3.1 Added failing dorny-count test (observed FAIL: 2 < 6)
- [x] 3.2 Evidence captured
- [x] 3.3 Mapped

## 4. Implementación

- [x] 4.1 Heuristic counts dorny/paths-filter
- [x] 4.2 Regenerate baseline
- [x] 4.3 REPO-SPLIT-PLAN P0.1 note
- [x] 4.4 CHANGELOG
- [x] 4.5 OpenSpec Gate 1
- [x] 4.6 Tests green

## 5. Actualizar tests existentes

- [x] 5.1 Extended test_repo_metrics.py
- [x] 5.2 workspace verify

## 6. Ejecutar regresión

- [x] 6.1 Backend n/a
- [x] 6.2 Coverage n/a
- [x] 6.3 validate + workspace verify
- [x] 6.4 No Disabled

## 7. Ejecutar Playwright

- [x] 7.1 n/a

## 8. Gate 3 — Documentación

- [x] 8.1 Baseline + plan
- [x] 8.2 CHANGELOG
- [x] 8.3 OpenSpec tasks

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 Refs full URL for #1256; keep #1445 open
- [x] 9.3 No secrets

## 10. Pull Request y validación CI

- [ ] 10.1 Push
- [ ] 10.2 Open draft PR
- [ ] 10.3 Wait CI
- [ ] 10.4 Gate 4

## 11. Deploy

- [ ] 11.1 Merge via PR
- [ ] 11.2 Confirm metrics on main

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke unit test
- [ ] 12.2 Rollback = revert
- [ ] 12.3 Keep #1445 open; do not invent Owner closes
- [ ] 12.4 Done

## Definition of Done

- [ ] Path-scoping metric ≥ 6 on main; #1445 remains open
