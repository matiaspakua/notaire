> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2202 exists and references CU76
- [x] 1.2 Use Case CU76 exists
- [x] 1.3 Acceptance criteria are the scenarios in the delta spec
- [x] 1.4 Impact analysis is in proposal.md
- [x] 1.5 No ADR: copy on the public site is not an architecture decision
- [x] 1.6 Issue stays open

## 2. Crear branch

- [x] 2.1 `cursor/pages-story-honesty-2277` from `origin/main`
- [x] 2.2 Recorded in traceability.md

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Story honesty cases
- [x] 3.2 Unit tests written
- [x] 3.3 No new HTTP integration tests
- [x] 3.4 Failing run observed before the copy change
- [x] 3.5 Mapped in traceability.md

## 4. Implementación

- [x] 4.1 Remove the unmeasured AI-work bar
- [x] 4.2 Say the legacy JPA package was retired
- [x] 4.3 Keep story cards visible. No fade-in.
- [x] 4.4 Spanish release-history lead

## 5. Actualizar tests existentes

- [x] 5.1 New tests cover the three claims
- [x] 5.2 No assertion weakened
- [x] 5.3 No test removed

## 6. Ejecutar regresión

- [x] 6.1 `bash workspace/sdlc/preflight.sh --full` before push
- [x] 6.2 Covered by preflight
- [x] 6.3 Covered by preflight
- [x] 6.4 Covered by preflight
- [x] 6.5 No new skipped tests

## 7. Ejecutar Playwright

- [x] 7.1 Existing pages site spec
- [x] 7.2 Pages Playwright via preflight
- [x] 7.3 375 and 1280
- [x] 7.4 Product Playwright is in preflight --full

## 8. Gate 3 — Documentación permanente

- [x] 8.1 CHANGELOG left to release PR #2230, the other Unreleased writer
- [x] 8.2 No API change
- [x] 8.3 Unreleased section
- [x] 8.4 Nothing for docs/000-archive
- [x] 8.5 Product behavior is described, not changed
- [x] 8.6 preflight.sh --full is the pull-request proof

## 9. Commits atómicos

- [x] 9.1 Conventional commits
- [x] 9.2 Messages reference #2202
- [x] 9.3 No secrets
- [x] 9.4 SHAs are on the branch

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR
- [ ] 10.2 Push
- [ ] 10.3 PR opened
- [ ] 10.4 Owner merges

## 11. Deploy

- [ ] 11.1 Pages deploy on merge to main

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 After merge, open the published site
- [ ] 12.2 Owner closes #2202

## Definition of Done

- [ ] Scenarios in the delta spec pass
- [ ] preflight.sh --full exits 0
- [ ] CHANGELOG updated
