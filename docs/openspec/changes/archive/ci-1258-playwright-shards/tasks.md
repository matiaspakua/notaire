> Groups 1-12 mandatory.

## 1. Gate 1 — Prerequisites
- [x] 1.1 #1258 + CU76; copy offline OpenSpec after #1257 lands
- [x] 1.2 validate openspec + sdlc-plan

## 2. Crear branch
- [x] 2.1 `cursor/ci-1258-playwright-shards-cf98`

## 3. Gate 2 — TDD
- [x] 3.1 Failing invariants for matrix + merge job

## 4. Implementación
- [x] 4.1 Matrix shards + blob/HTML artifacts
- [ ] 4.2 Merge-reports fail-closed
- [x] 4.3 Aggregator wiring

## 5. Actualizar tests existentes
- [x] 5.1 Invariants green

## 6. Ejecutar regresión
- [x] 6.1 Full Playwright green on PR

## 7. Ejecutar Playwright
- [x] 7.1 Sharded suite green; report merged

## 8. Gate 3 — Documentación
- [x] 8.1 Testing docs + CHANGELOG

## 9. Commits atómicos
- [x] 9.1 `Closes #1258` `Refs #1197`

## 10. Pull Request y validación CI
- [ ] 10.1 Heavy CI + check-heavy-ci

## 11. Deploy
- [ ] 11.1 Merge

## 12. Gate 5 — Smoke test y cierre
- [ ] 12.1 Wall-clock note; close issue

## Definition of Done
- [x] E2E wall ≤6 min evidenced
- [ ] All gates passed
