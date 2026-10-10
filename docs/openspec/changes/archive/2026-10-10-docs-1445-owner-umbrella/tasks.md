> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). Groups 1-12 mandatory.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1445 + CU76
- [x] 1.2 Use Case exists
- [x] 1.3 AC ()
- [x] 1.4 Impact Analysis
- [x] 1.5 ADR-024 pointer update
- [ ] 1.6 Label IN PROGRESS — 403 likely

## 2. Crear branch

- [x] 2.1 Fetch main
- [x] 2.2 
- [x] 2.3 Recorded
- [x] 2.4 validate-sdlc-plan

## 3. Gate 2 — Tests first

- [x] 3.1 Update failing/green guard
- [x] 3.2 Observed
- [x] 3.3 Mapped

## 4. Implementación

- [x] 4.1 ADR-024 → #1445
- [x] 4.2 REPO-SPLIT-PLAN → #1445
- [x] 4.3 CHANGELOG
- [x] 4.4 Guard green

## 5. Actualizar tests existentes

- [x] 5.1 Guard green
- [x] 5.2 Related guards green

## 6. Ejecutar regresión

- [x] 6.1 Backend n/a
- [x] 6.2 Coverage n/a
- [x] 6.3 validate + unittest
- [x] 6.4 No Disabled

## 7. Ejecutar Playwright

- [x] 7.1 n/a docs only

## 8. Gate 3 — Documentación

- [x] 8.1 ADR-024 / plan
- [x] 8.2 CHANGELOG
- [x] 8.3 OpenSpec

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 Refs only (full URL preferred)
- [ ] 9.3 No secrets

## 10. Pull Request y validación CI

- [ ] 10.1 Push
- [ ] 10.2 Open draft PR
- [ ] 10.3 Wait CI
- [ ] 10.4 Gate 4

## 11. Deploy

- [ ] 11.1 Merge via PR
- [ ] 11.2 Confirm on main

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke ADR-024 cites #1445
- [ ] 12.2 Rollback = revert
- [ ] 12.3 Keep #1445 open
- [ ] 12.4 Archive later

## Definition of Done

- [ ] #1445 linked; docs point at it; issue remains open after merge
