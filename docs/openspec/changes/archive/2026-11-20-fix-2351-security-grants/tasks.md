> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2351 is open
- [x] 1.2 Acceptance criteria are the two scenarios in the delta spec
- [x] 1.3 Impact Analysis is in proposal.md
- [x] 1.4 No ADR

## 2. Crear branch

- [x] 2.1 Branch `cursor/2351-security-grants-47dc` from `origin/main`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Authority tests use a domain user query
- [x] 3.2 Grant tests use domain user and role queries

## 4. Implementación

- [x] 4.1 UserAuthorityResolver reads QueryUsersUseCase
- [x] 4.2 RoleModuleGrantService reads QueryUsersUseCase and QueryRolesUseCase
- [x] 4.3 Neither service imports business.User or business.Role

## 5. Actualizar tests existentes

- [x] 5.1 The notary fallback and inactive-user cases still pass

## 6. Ejecutar regresión

- [x] 6.1 The two unit tests passed
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI change

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 No permanent product doc beyond this change
- [x] 8.2 No API shape change

## 9. Commits atómicos

- [ ] 9.1 Conventional commit
- [ ] 9.2 The issue stays open, so the message does not use a closing keyword

## 10. Pull Request y validación CI

- [ ] 10.1 Archive, push, open the PR. Refs #2351

## 11. Deploy

- [ ] 11.1 Merge only when preflight exited 0 and required checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 The issue stays open for audit, report, and seed imports

## Definition of Done

- [x] Issue linked to a Use Case
- [x] Specification written
- [x] Unit tests passed
- [ ] Full suite green via preflight
- [ ] Commit does not close #2351
- [ ] PR created
- [ ] Issue stays open after merge
