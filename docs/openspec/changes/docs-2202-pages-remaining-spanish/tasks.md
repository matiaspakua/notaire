# Tasks — docs-2202-pages-remaining-spanish

## 1. Gate 1 — Prerequisites

- [x] 1.1 Confirm Issue #2202 + Use Case CU76
- [x] 1.2 Seed OpenSpec change via `seed-openspec-change.sh` (#2218 path)
- [x] 1.3 Fill proposal, design, tasks, traceability, delta spec
- [ ] 1.4 `openspec validate` + `bash workspace/sdlc/validate-sdlc-plan.sh`
- [x] 1.5 Gate 1.6 WIP: `add-issue-wip-label.sh 2202` (honest SKIP if 403)

## 2. Crear branch

- [x] 2.1 Branch `docs/2202_pages-remaining-spanish` from updated `main` (includes #2235)
- [x] 2.2 Mirror tip on `cursor/2202-pages-remaining-spanish-f3a4` for PR open (#2233)

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Extend `i18n-coverage.test.ts` for anatomy figures + Mermaid/caption keys
- [x] 3.2 Anatomy unit test for Spanish plate/readout under PreferencesProvider
- [x] 3.3 Playwright SPANISH samples for `/product/anatomy/` and `/ai-sdlc/lifecycle/`

## 4. Implementación

- [x] 4.1 `strings-anatomy.ts` + `strings-mermaid.ts` wired from `strings.ts`
- [x] 4.2 `AnatomyFigure` applies `tr(locale, …)` to plate, steps, readout
- [x] 4.3 SiteFooter docs titles via `factoryPageTitle`; DocShell “AI Factory chapters”

## 5. Actualizar tests existentes

- [ ] 5.1 Confirm English anatomy step tests still pass without PreferencesProvider

## 6. Ejecutar regresión

- [ ] 6.1 `npm test` in `github-page/`
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [ ] 7.1 Pages Playwright preferences Spanish cases (inside preflight `--full`)

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update `github-page/README.md` Spanish note if needed
- [ ] 8.2 CHANGELOG only if Unreleased fence free
- [ ] 8.3 Archive OpenSpec; sync main `github-pages-site-shell` delta

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits on constitutional branch
- [ ] 9.2 Push `docs/2202_pages-remaining-spanish` and `cursor/…-f3a4`

## 10. Pull Request y validación CI

- [ ] 10.1 Prefer `gh pr create`; on createPullRequest 403 use ManagePullRequest on `cursor/*`
- [ ] 10.2 PR title `[#2202] …` with `Closes #2202`
- [ ] 10.3 Workers never merge

## 11. Deploy

- [ ] 11.1 Owner merges; Pages publish on main

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 After merge, smoke Spanish anatomy heading on published Pages (Owner)

## Definition of Done

- [ ] Preflight `--full` PASS
- [ ] PR open with `Closes #2202`
- [ ] OpenSpec archived
- [ ] Handoff at `internal/asli-71-handoff.md`
