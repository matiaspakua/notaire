> Groups 1-12 mandatory.

## 1. Gate 1 — Prerequisites
- [ ] 1.1 #1260 + CU76; copy OpenSpec tree
- [ ] 1.2 validate openspec + sdlc-plan

## 2. Crear branch
- [ ] 2.1 `cursor/feat-1260-openapi-ts-types-cf98`

## 3. Gate 2 — TDD
- [ ] 3.1 Failing drift check / type import test before committing generated file wiring

## 4. Implementación
- [ ] 4.1 openapi-typescript + scripts
- [ ] 4.2 Generate + commit api.generated.ts
- [ ] 4.3 Frontend CI drift step
- [ ] 4.4 Migrate dashboard, gestiones, documentos, presupuestos hooks

## 5. Actualizar tests existentes
- [ ] 5.1 typecheck + Vitest green

## 6. Ejecutar regresión
- [ ] 6.1 frontend-ci locally / CI

## 7. Ejecutar Playwright
- [ ] 7.1 Product suite (frontend touched)

## 8. Gate 3 — Documentación
- [ ] 8.1 design doc + frontend README + CHANGELOG

## 9. Commits atómicos
- [ ] 9.1 `Closes #1260` `Refs #1197`

## 10. Pull Request y validación CI
- [ ] 10.1 Heavy CI / check-heavy-ci

## 11. Deploy
- [ ] 11.1 Merge

## 12. Gate 5 — Smoke test y cierre
- [ ] 12.1 Confirm drift check fails when yaml drifts; close issue

## Definition of Done
- [ ] One-command regen; CI drift; four hook areas migrated
- [ ] All gates passed
