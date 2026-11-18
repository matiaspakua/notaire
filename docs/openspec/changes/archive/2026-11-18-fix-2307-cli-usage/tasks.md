> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2307 exists
- [x] 1.2 CU76 already exists
- [x] 1.3 This change is the CLI usage log and the mcp 1 pin
- [x] 1.4 Impact analysis is in `proposal.md`
- [x] 1.5 No new ADR
- [x] 1.6 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Updated `main`
- [x] 2.2 Branch `cursor/2307-cli-usage-47dc`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Report counts a CLI search and an impact
- [x] 3.2 Test written first
- [x] 3.3 Observed `FileNotFoundError` for `usage.jsonl`

## 4. Implementación

- [x] 4.1 CLI query commands call `record`
- [x] 4.2 Report counts `src=cli`
- [x] 4.3 Pin `mcp<2`

## 5. Actualizar tests existentes

- [x] 5.1 Existing CLI tests still pass
- [x] 5.2 No test removed

## 6. Ejecutar regresión

- [x] 6.1 `verify.sh` exit 0 (69 passed, coverage 91.10%, wheel `repolens-0.4.1`, smoke OK)
- [ ] 6.2 `preflight.sh --full` exit 0 before push

## 7. Ejecutar Playwright

- [x] 7.1 No product UI change

## 8. Gate 3 — Documentación permanente

- [x] 8.1 CHANGELOG 0.4.1
- [x] 8.2 Factory comparison and learning log

## 9. Commits atómicos

- [x] 9.1 Conventional commit

## 10. Pull Request y validación CI

- [ ] 10.1 PR opened with `Refs #2307`

## 11. Deploy

- [ ] 11.1 Owner merges

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 `verify.sh` is the smoke
- [ ] 12.2 Do not close #2307 until the impact over-link is also fixed or split

## Definition of Done

- [ ] CLI search writes the usage log
- [ ] `verify.sh` exit 0
- [ ] `preflight.sh --full` exit 0
- [ ] PR open
