> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issues #1414–#1417 created and linked to CU76
- [x] 1.2 Use Case CU76 linked
- [x] 1.3 Acceptance Criteria as scenarios in delta specs
- [x] 1.4 Impact Analysis in `proposal.md`
- [x] 1.5 ADR-027 in scope
- [ ] 1.6 Move Issues to IN PROGRESS — blocked (label API 403)

## 2. Crear branch

- [x] 2.1 Fetch `origin/main`
- [x] 2.2 Branch `cursor/docs-1197-pages-modules-cf98`
- [x] 2.3 Branch recorded in `traceability.md`
- [ ] 2.4 `bash workspace/sdlc/validate-sdlc-plan.sh docs-1414-pages-tech-docs`

## 3. Gate 2 — Verification (TDD where applicable)

- [ ] 3.1 Write failing `workspace/tests/test_repo_metrics.py`
- [ ] 3.2 Observe fail before implementation
- [ ] 3.3 Map each Scenario to a verification step

## 4. Implementación

- [ ] 4.1 `workspace/ci/repo-metrics.py` + baseline Markdown
- [ ] 4.2 `MODULE-OWNERSHIP.md` + Mermaid map
- [ ] 4.3 ADR-027 + diagrams README + SAD Project #1 fix
- [ ] 4.4 GitHub Pages Docs routes + Nav + Mermaid diagrams
- [ ] 4.5 Index / REPO-SPLIT-PLAN / CHANGELOG links
- [ ] 4.6 Optional: fleet note for Phase 0 priority

## 5. Actualizar tests existentes

- [ ] 5.1 Metrics unit test green
- [ ] 5.2 `test_docs_links` / `test_modules_manifest` still green
- [ ] 5.3 `cd github-page && npm ci && npm run build`

## 6. Ejecutar regresión

- [ ] 6.1–6.5 n/a product backend (docs/Pages only)

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no product UI
- [ ] 7.2 Pages verified via Next build (+ post-merge deploy smoke)

## 8. Gate 3 — Documentación permanente

- [ ] 8.1 All permanent docs listed in proposal
- [ ] 8.2 Indexes updated
- [ ] 8.3 CHANGELOG Unreleased

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 Final commit / PR body `Closes #1414` `#1415` `#1416` `#1417`; `Refs #1197` `#1256` `#921`
- [ ] 9.3 No secrets

## 10. Pull Request y validación CI

- [ ] 10.1 Push branch
- [ ] 10.2 Open PR
- [ ] 10.3 Wait for required workflows
- [ ] 10.4 Gate 4 — CI green

## 11. Deploy

- [ ] 11.1 Human merges via PR
- [ ] 11.2 Confirm Pages deploy on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke Docs tab on live Pages
- [ ] 12.2 Rollback = revert PR
- [ ] 12.3 Issues auto-close via `Closes`
- [ ] 12.4 Archive OpenSpec change

## Definition of Done

- [ ] Issues + CU76 + Gate 1 artifacts
- [ ] Metrics test red-then-green
- [ ] Docs + Pages + ADR shipped
- [ ] PR with closing keywords; CI green
