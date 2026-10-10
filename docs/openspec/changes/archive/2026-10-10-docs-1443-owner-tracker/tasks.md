> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). Groups 1-12 mandatory.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1443 + CU76
- [x] 1.2 Use Case exists
- [x] 1.3 AC in proposal (`skip_specs`)
- [x] 1.4 Impact Analysis
- [x] 1.5 ADR — update ADR-024 pointers only
- [ ] 1.6 IN PROGRESS label — 403 likely

## 2. Crear branch

- [x] 2.1 Fetch main
- [x] 2.2 `cursor/docs-1197-owner-tracker-reopen-cf98`
- [x] 2.3 Recorded in traceability
- [x] 2.4 validate-sdlc-plan.sh

## 3. Gate 2 — Tests first

- [x] 3.1 Failing `test_adr024_owner_tracker.py`
- [x] 3.2 Observed FAIL
- [x] 3.3 Assertions map to AC

## 4. Implementación

- [x] 4.1 ADR-024 Deciders + auto-close note → #1443
- [x] 4.2 REPO-SPLIT-PLAN issue map umbrella → #1443
- [x] 4.3 CHANGELOG
- [x] 4.4 Guard green

## 5. Actualizar tests existentes

- [x] 5.1 New guard green
- [x] 5.2 adr022 guard still green

## 6. Ejecutar regresión

- [x] 6.1 Backend n/a
- [x] 6.2 Coverage n/a
- [x] 6.3 validate-sdlc-plan + unittest
- [x] 6.4 No Disabled

## 7. Ejecutar Playwright

- [x] 7.1 n/a docs only

## 8. Gate 3 — Documentación

- [x] 8.1 ADR-024 / REPO-SPLIT-PLAN
- [x] 8.2 CHANGELOG
- [x] 8.3 OpenSpec tasks

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 `Refs #1443` only (do not close tracker)
- [x] 9.3 No secrets

## 10. Pull Request y validación CI

- [x] 10.1 Push
- [x] 10.2 Open draft PR
- [ ] 10.3 Wait CI
- [ ] 10.4 Gate 4

## 11. Deploy

- [ ] 11.1 Merge via PR
- [ ] 11.2 Confirm docs on main

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: ADR-024 on main cites #1443
- [ ] 12.2 Rollback = revert
- [ ] 12.3 Keep #1443 OPEN
- [ ] 12.4 Archive OpenSpec later

## Definition of Done

- [ ] #1443 linked; Gate 1 complete
- [ ] Guard red-then-green
- [ ] Docs point at #1443; #1443 stays open
