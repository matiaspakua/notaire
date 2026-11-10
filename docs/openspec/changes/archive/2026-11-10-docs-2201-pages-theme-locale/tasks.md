> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2201 exists and references CU76
- [x] 1.2 Use Case CU76 exists
- [x] 1.3 Acceptance criteria are the scenarios in the delta specs
- [x] 1.4 Impact analysis is in proposal.md
- [x] 1.5 No ADR: the Pages theme is not an architecture decision
- [x] 1.6 Issue labeled status:in-progress

## 2. Crear branch

- [x] 2.1 Branch from updated main
- [x] 2.2 Branch `cursor/pages-theme-locale-2277` (cloud agent branch template)
- [x] 2.3 Recorded in traceability.md

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Cases: theme cycle, language resolution, contrast, reading order, inventory, allow list, Spanish nav
- [x] 3.2 Unit tests written
- [x] 3.3 No new HTTP integration tests (static site)
- [x] 3.4 Failing run observed before the implementation (missing modules)
- [x] 3.5 Each delta scenario maps to a test in traceability.md

## 4. Implementación

- [x] 4.1 Preference boot script, provider, header controls
- [x] 4.2 Dark theme CSS overrides
- [x] 4.3 Spanish dictionary for the shell, home, and factory introduction
- [x] 4.4 Reading path and framework status panel
- [x] 4.5 Untranslated-page notice
- [x] 4.6 Playwright preferences spec

## 5. Actualizar tests existentes

- [x] 5.1 Header tests stay English by default
- [x] 5.2 No assertion weakened
- [x] 5.3 No test removed

## 6. Ejecutar regresión

- [x] 6.1 `bash workspace/sdlc/preflight.sh --full` (includes the product suite)
- [x] 6.2 Covered by preflight
- [x] 6.3 Covered by preflight
- [x] 6.4 Covered by preflight
- [x] 6.5 No new skipped tests

## 7. Ejecutar Playwright

- [x] 7.1 `github-page/e2e/preferences.spec.ts` plus the existing pages smoke
- [x] 7.2 Pages Playwright via preflight
- [x] 7.3 375 and 1280 are the pages projects
- [x] 7.4 Product Playwright is included in preflight --full

## 8. Gate 3 — Documentación permanente

- [x] 8.1 github-page/README.md
- [x] 8.2 No API change
- [x] 8.3 CHANGELOG.md [Unreleased]
- [x] 8.4 Nothing to archive in docs/000-archive
- [x] 8.5 Kit behavior stays sourced from the README and the TOML files
- [x] 8.6 `bash workspace/sdlc/preflight.sh --full` is the pull-request proof for this change

## 9. Commits atómicos

- [ ] 9.1 Conventional commits
- [ ] 9.2 Messages reference #2201
- [ ] 9.3 No secrets
- [ ] 9.4 SHAs recorded in traceability.md

## 10. Pull Request y validación CI

- [ ] 10.1 Archive with `openspec archive` unless the PR is still open at handoff; archive in the PR before merge
- [ ] 10.2 Push the branch
- [ ] 10.3 PR `[#2201] docs(pages): dark mode, Spanish, and an accurate factory path`
- [ ] 10.4 Owner merges. This agent does not merge.

## 11. Deploy

- [ ] 11.1 Deploy is the existing Pages workflow on merge to main
- [ ] 11.2 No trigger or ruleset change

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 After merge, the Owner (or the next agent) opens the published site
- [ ] 12.2 Close #2201 when the PR merges

## Definition of Done

- [ ] Scenarios in the delta specs pass
- [ ] preflight.sh --full exits 0
- [ ] CHANGELOG and README updated
- [ ] PR open, Owner merges
