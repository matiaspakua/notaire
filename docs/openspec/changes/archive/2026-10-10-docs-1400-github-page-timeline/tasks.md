> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1400 exists (full body at create; edit API 403)
- [x] 1.2 Use Case CU76 linked in proposal / issue body
- [x] 1.3 Acceptance Criteria as scenarios in the delta spec
- [x] 1.4 Impact Analysis in `proposal.md`
- [x] 1.5 ADR — n/a
- [ ] 1.6 Move Issue to IN PROGRESS — **blocked**: `addLabelsToLabelable` 403 for integration token (admin)

## 2. Crear branch

- [x] 2.1 Fetch `origin/main`
- [x] 2.2 Branch `cursor/docs-1400-github-page-timeline-c19f`
- [x] 2.3 Branch recorded in `traceability.md`
- [x] 2.4 OpenSpec artifacts present under `docs/openspec/changes/docs-1400-github-page-timeline/`
- [x] 2.5 `bash workspace/sdlc/validate-sdlc-plan.sh docs-1400-github-page-timeline` (run before push)

## 3. Gate 2 — Verification (docs-only)

- [x] 3.1 Enumerate checks: prior eras intact, new era present, workflow untouched, build
- [ ] 3.2 Observe fail: timeline missing Sept–Oct (pre-change) — documented by audit
- [x] 3.3 Map each Scenario to a verification step

## 4. Implementación

- [x] 4.1 Append Sept–Oct 2026 event(s) to `github-page/components/Timeline.tsx`
- [x] 4.2 Update `AIEra` workflow banner for OpenSpec / Cloud fleet (English)
- [x] 4.3 Add Cursor Cloud to `AITools` without removing prior tools
- [x] 4.4 Append Flyway resolution note on schema challenge in `FunFacts.tsx`
- [x] 4.5 `CHANGELOG.md` Unreleased entry

## 5. Actualizar tests existentes

- [x] 5.1 n/a product tests
- [x] 5.2 `github-page` `npm ci && npm run build`

## 6. Ejecutar regresión

- [ ] 6.1–6.5 n/a (no backend/API change)

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no product UI change
- [ ] 7.2 Pages site verified via Next build + post-merge deploy

## 8. Gate 3 — Documentación permanente

- [ ] 8.1 CHANGELOG
- [ ] 8.2 Store sanity report (not in repo)
- [ ] 8.3–8.5 n/a archive

## 9. Commit

- [ ] 9.1 Conventional Commits + `Closes #1400`

## 10. Push + PR

- [x] 10.1 Push branch
- [x] 10.2 Draft PR (do not merge) — https://github.com/matiaspakua/notaire/pull/1402
- [x] 10.3 Record PR number in `traceability.md`
- [ ] 10.4 Wait for CI on the draft PR
- [ ] 10.5 Gate 4 — CI green, review approved (human)
- [ ] 10.6 Do not merge from the agent

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `Deploy GitHub Page` ran after CI on `main`
- [ ] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: open https://matiaspakua.github.io/notaire/ — prior eras + Sept–Oct 2026 entry visible
- [ ] 12.2 Verify rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1400 referencing the PR; close probe #1399
- [ ] 12.4 Archive the change: `cd docs && openspec archive docs-1400-github-page-timeline`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Verification designed (Gate 2)
- [ ] `github-page` build passes; deploy workflow untouched
- [ ] Prior timeline eras preserved; new era appended
- [ ] Permanent documentation updated (`CHANGELOG.md`)
- [ ] Conventional Commits with `Closes #1400`
- [ ] Draft Pull Request created (Gate 4 pending human)
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
