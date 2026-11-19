> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2329 and CU76
- [x] 1.2 The ratchet has one line left
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch from `origin/main` at `5e52fe1e`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Entity test calls the mapper first
- [x] 3.2 Compile failed: class missing

## 4. Implementación

- [x] 4.1 Move `getDto` to `DeedManagementLegacyMapper.toDto`
- [x] 4.2 Empty the ratchet baseline

## 5. Actualizar tests existentes

- [x] 5.1 Null-safety tests call the mapper

## 6. Ejecutar regresión

- [x] 6.1 Entity and ratchet tests
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Documentación permanente

- [x] 8.1 No product document changes

## 9. Commits atómicos

- [x] 9.1 Conventional commit

## 10. Pull Request y validación CI

- [ ] 10.1 Archive, then open the PR. This slice finishes the issue.

## 11. Deploy

- [ ] 11.1 Merge when preflight exit 0 and checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 The issue is finished when the empty baseline is on main.

## Definition of Done

- [x] Failing test observed
- [ ] preflight.sh --full exit 0
- [ ] PR open and the last mapping method removed
