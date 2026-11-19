> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2336 and CU76
- [x] 1.2 Scope is payload size and the blocking refresh
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch from `origin/main` at `7c79f9a4`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Refresh test expects no `refreshed in` on the first search
- [x] 3.2 It failed on the old blocking stderr

## 4. Implementación

- [x] 4.1 Defaults: 4 detailed hits, 4 one-line hits, 32-line snippet, impact depth 2
- [x] 4.2 Stale query spawns a detached index and returns

## 5. Actualizar tests existentes

- [x] 5.1 Usage-log test clears the shared log first

## 6. Ejecutar regresión

- [x] 6.1 The three pytest cases passed
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no product UI

## 8. Gate 3 — Documentación permanente

- [x] 8.1 AGENTS.md, changelog, KPI and learning log

## 9. Commits atómicos

- [x] 9.1 Conventional commit

## 10. Pull Request y validación CI

- [ ] 10.1 Archive, then open the PR. #2336 stays open.

## 11. Deploy

- [ ] 11.1 Merge when preflight exit 0 and checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 This slice is done when the defaults are on main. The issue stays open.

## Definition of Done

- [x] Failing test observed
- [ ] preflight.sh --full exit 0
- [ ] PR open
