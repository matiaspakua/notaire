> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). Groups 1-12 mandatory.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1445 + CU76
- [x] 1.2 Use Case exists
- [x] 1.3 AC (`skip_specs`)
- [x] 1.4 Impact Analysis
- [x] 1.5 n/a new ADR (update ADR-024 evidence only)
- [ ] 1.6 Label — 403 likely

## 2. Crear branch

- [x] 2.1 Fetch main
- [x] 2.2 Branch created
- [x] 2.3 Recorded
- [x] 2.4 validate-sdlc-plan

## 3. Gate 2 — Tests first

- [x] 3.1 Added failing unit tests (observed FAIL)
- [x] 3.2 Evidence captured
- [x] 3.3 Mapped

## 4. Implementación

- [x] 4.1 Architecture Owner pack
- [x] 4.2 Modules links
- [x] 4.3 ADR-024 evidence row
- [x] 4.4 Archive docs-1445-archive-1458-hygiene
- [x] 4.5 CHANGELOG + OpenSpec
- [x] 4.6 Tests green

## 5. Actualizar tests existentes

- [x] 5.1 Extended test_adr024_owner_tracker.py
- [x] 5.2 workspace verify

## 6. Ejecutar regresión

- [x] 6.1 Backend n/a
- [x] 6.2 Coverage n/a
- [x] 6.3 validate + verifies
- [x] 6.4 No Disabled

## 7. Ejecutar Playwright

- [x] 7.1 n/a (unit guards)

## 8. Gate 3 — Documentación

- [x] 8.1 Pages + ADR
- [x] 8.2 CHANGELOG
- [x] 8.3 OpenSpec tasks

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 Refs full URL only
- [x] 9.3 No secrets

## 10. Pull Request y validación CI

- [ ] 10.1 Push
- [ ] 10.2 Open draft PR
- [ ] 10.3 Wait CI
- [ ] 10.4 Gate 4

## 11. Deploy

- [ ] 11.1 Merge via PR
- [ ] 11.2 Confirm Pages Deploy shows Phase 0 + 6/17

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke unit tests
- [ ] 12.2 Rollback = revert
- [ ] 12.3 Keep #1445 open
- [ ] 12.4 Done

## Definition of Done

- [ ] Live Pages show P0.1–P0.5 Done and 6/17; #1445 remains open
