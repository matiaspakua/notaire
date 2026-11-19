> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2331 and CU76
- [x] 1.2 Scope is the stale-index query path
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch from updated main

## 3. Gate 2 — Escribir tests

- [x] 3.1 Test written first
- [x] 3.2 It returned no results and no stderr warning

## 4. Implementación

- [x] 4.1 Stamp indexed_head
- [x] 4.2 Refresh before a read query when HEAD differs
- [x] 4.3 MCP forces a background refresh when the same check is stale

## 5. Actualizar tests existentes

- [x] 5.1 Background refresh and CLI usage tests still pass
- [x] 5.2 Version 0.4.5 and changelog

## 6. Ejecutar regresión

- [x] 6.1 Targeted pytest passed
- [ ] 6.2 verify.sh and preflight.sh --full

## 7. Ejecutar Playwright

- [x] 7.1 No product UI

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Changelog, README, AGENTS.md, factory notes

## 9. Commits atómicos

- [x] 9.1 Conventional commit

## 10. Pull Request y validación CI

- [ ] 10.1 PR with Refs #2331

## 11. Deploy

- [ ] 11.1 Merge when preflight and checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 This issue is finished when the slice merges

## Definition of Done

- [x] Failing test observed
- [ ] preflight.sh --full exit 0
- [ ] PR open
