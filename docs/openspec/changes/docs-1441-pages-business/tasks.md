> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1441 exists, linked to CU76 / parent #1197
- [x] 1.2 Use Case CU76 exists
- [x] 1.3 Acceptance Criteria in proposal/traceability (`skip_specs` justified)
- [x] 1.4 Impact Analysis in `proposal.md`
- [x] 1.5 ADR — n/a (Pages content extension)
- [ ] 1.6 Move Issue to IN PROGRESS — label API often 403; branch link in PR

## 2. Crear branch

- [x] 2.1 Fetch `origin/main`
- [x] 2.2 Branch `cursor/docs-1441-pages-business-cf98`
- [x] 2.3 Branch recorded in `traceability.md`
- [x] 2.4 `bash workspace/sdlc/validate-sdlc-plan.sh docs-1441-pages-business`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Write failing `workspace/tests/test_pages_business_docs.py`
- [x] 3.2 Observe FAIL before implementation
- [x] 3.3 Map each Acceptance Criterion to an assertion

## 4. Implementación

- [x] 4.1 `github-page/app/docs/business/page.tsx` with deep-links
- [x] 4.2 DocsChrome nav + home card
- [x] 4.3 CHANGELOG Unreleased
- [x] 4.4 Unit guard green

## 5. Actualizar tests existentes

- [x] 5.1 New guard green
- [x] 5.2 `test_adr022_owner_decision_pack` still green
- [x] 5.3 `cd github-page && npm run build` (or CI Frontend/Pages path)

## 6. Ejecutar regresión

- [ ] 6.1 Backend — n/a
- [ ] 6.2 Coverage — n/a
- [x] 6.3 `validate-sdlc-plan.sh` + unit tests for this change
- [ ] 6.4 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 Product Playwright — n/a (no `frontend/` change)
- [ ] 7.2 Pages smoke after deploy (200 on `/docs/business/`)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 CHANGELOG Unreleased
- [x] 8.2 Business Markdown trees unchanged (deep-link only)
- [x] 8.3 OpenSpec tasks/traceability updated

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 PR body `Closes #1441`; `Refs #1197` (do not close #1197)
- [ ] 9.3 No secrets

## 10. Pull Request y validación CI

- [ ] 10.1 Push branch
- [ ] 10.2 Open draft PR
- [ ] 10.3 Wait for required workflows / heavy CI
- [ ] 10.4 Gate 4 — CI green

## 11. Deploy

- [ ] 11.1 Merge via PR (never push main)
- [ ] 11.2 Confirm Pages deploy after main CI

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke `/notaire/docs/business/` shows Business heading
- [ ] 12.2 Rollback = revert PR
- [ ] 12.3 Issue #1441 closes via keyword (agents may get 403 on manual close)
- [ ] 12.4 Archive OpenSpec change after merge

## Definition of Done

- [ ] Issue #1441 + CU76 + Gate 1 artifacts
- [ ] Unit guard red-then-green
- [ ] Business Docs page + nav + card shipped
- [ ] PR CI green; Pages smoke after deploy
