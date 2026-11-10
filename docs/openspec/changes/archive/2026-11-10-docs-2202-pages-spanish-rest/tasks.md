> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2202 exists and references CU76
- [x] 1.2 Use Case CU76 exists
- [x] 1.3 Acceptance criteria are the scenarios in the delta spec
- [x] 1.4 Impact analysis is in proposal.md
- [x] 1.5 No ADR: copy and a dictionary are not an architecture decision
- [x] 1.6 Issue label update was denied to this token (403). Reported in the PR.

## 2. Crear branch

- [x] 2.1 Branch from updated main (`6816cd7f`, #2206 merged)
- [x] 2.2 Branch `cursor/pages-spanish-rest-2277`
- [x] 2.3 Recorded in traceability.md

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Cases: translated-route set, content-module Spanish keys, Spanish headings in light and dark at 375 and 1280
- [x] 3.2 Unit tests written (`i18n-coverage.test.ts`)
- [x] 3.3 No new HTTP integration tests (static site)
- [x] 3.4 The coverage test fails when a content-module string has no Spanish entry
- [x] 3.5 Each delta scenario maps to a test in traceability.md

## 4. Implementación

- [x] 4.1 Dictionary keyed by the English sentence
- [x] 4.2 Walker so a page does not fork
- [x] 4.3 Sidebar titles and the demo banner
- [x] 4.4 Notice removed only on translated routes

## 5. Actualizar tests existentes

- [x] 5.1 `/docs/hexagonal/` still expects the English notice
- [x] 5.2 No assertion weakened
- [x] 5.3 No test removed

## 6. Ejecutar regresión

- [x] 6.1 `bash workspace/sdlc/preflight.sh --full` before push
- [x] 6.2 Covered by preflight
- [x] 6.3 Covered by preflight
- [x] 6.4 Covered by preflight
- [x] 6.5 No new skipped tests

## 7. Ejecutar Playwright

- [x] 7.1 `github-page/e2e/preferences.spec.ts`
- [x] 7.2 Pages Playwright via preflight
- [x] 7.3 375 and 1280
- [x] 7.4 Product Playwright is included in preflight --full

## 8. Gate 3 — Documentación permanente

- [x] 8.1 github-page/README.md
- [x] 8.2 No API change
- [x] 8.3 CHANGELOG.md [Unreleased]
- [x] 8.4 Nothing to archive in docs/000-archive
- [x] 8.5 No kit behavior change
- [x] 8.6 `bash workspace/sdlc/preflight.sh --full` is the pull-request proof

## 9. Commits atómicos

- [x] 9.1 Conventional commits
- [x] 9.2 Messages reference #2202
- [x] 9.3 No secrets
- [x] 9.4 SHA recorded in traceability.md after the commit

## 10. Pull Request y validación CI

- [ ] 10.1 Archive with `openspec archive` in this PR
- [ ] 10.2 Push the branch
- [ ] 10.3 PR `[#2202] docs(pages): Spanish for the remaining public pages`
- [ ] 10.4 Owner merges. This agent does not merge.

## 11. Deploy

- [ ] 11.1 Pages deploy on main. This change does not deploy itself.

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 After merge, the Owner (or the next agent) opens the published site
- [ ] 12.2 Close #2202 when the PR merges

## Definition of Done

- [ ] Scenarios in the delta spec pass
- [ ] preflight.sh --full exits 0
- [ ] CHANGELOG and README updated
- [ ] PR open, Owner merges
