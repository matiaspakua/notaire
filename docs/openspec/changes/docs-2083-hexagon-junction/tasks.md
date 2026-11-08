> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2083 exists and names RNF-05
- [x] 1.2 Use Case RNF-05 (aspecto visual) exists
- [x] 1.3 Acceptance criteria are the scenarios in the delta spec
- [x] 1.4 Impact analysis is in proposal.md
- [x] 1.5 No ADR: the figure explains ADR-030 and does not change it
- [x] 1.6 Issue was already closed by #2084; this change corrects the figure

## 2. Crear branch

- [x] 2.1 Branch from current `main` (skills commit already merged)
- [x] 2.2 `feat/2083_hexagon-anatomy-figure`
- [x] 2.3 Branch name recorded in traceability.md

## 3. Gate 2 — Escribir tests

- [x] 3.1 Cases: glands and adapters, skin clearance, part count, keyboard readout, factory link
- [x] 3.2 Vitest in `artifacts.test.ts` and `hexagon-figure.test.tsx`
- [x] 3.3 No integration test: the page is static
- [x] 3.4 Geometry tests fail if a cable enters the case or the readout drops the dependency rule
- [x] 3.5 Each scenario below maps to a test

## 4. Implementación

- [x] 4.1 Junction geometry, live tour and plate
- [x] 4.2 Mount it on `/docs/hexagonal/` after the stat grid
- [x] 4.3 Factory home: one sentence and a link to `/ai-sdlc/lifecycle/`, no second gated line
- [x] 4.4 Remove the plug-ring figure

## 5. Actualizar tests existentes

- [x] 5.1 Replace the manifold assertions with the junction assertions
- [x] 5.2 `npm test` in `github-page` green

## 6. Ejecutar regresión

- [x] 6.1 `cd github-page && npm test`

## 7. Ejecutar Playwright

- [x] 7.1 Existing route sweep covers both pages; no new spec

## 8. Gate 3 — Documentación permanente

- [x] 8.1 CHANGELOG Unreleased line
- [x] 8.2 The public pages are the explanation

## 9. Commits atómicos

- [x] 9.1 Conventional commit, Refs #2083

## 10. Pull Request y validación CI

- [ ] 10.1 Open the PR
- [ ] 10.2 Required checks
- [ ] 10.3 Review

## 11. Deploy

- [ ] 11.1 Merge via the pull request
- [ ] 11.2 Pages publish with the site
- [ ] 11.3 Record the merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Open `/docs/hexagonal/` and `/ai-sdlc/` on the published site
- [ ] 12.2 Revert is the rollback
- [ ] 12.3 Issue already closed by #2084
- [ ] 12.4 Record the merge in traceability.md

## Definition of Done

- [x] Issue linked to RNF-05, with acceptance scenarios
- [x] Specification written
- [x] Tests written and green
- [x] github-page unit suite green
- [ ] Coverage floors unchanged (no Java)
- [x] Playwright strategy recorded; existing route sweep covers the pages
- [x] CHANGELOG updated
- [ ] Commit and PR
- [ ] CI green
- [ ] Merged

## Deviations

- #2084 already merged a ring-of-plugs figure and a second gated line, and closed #2083. This change replaces that figure with the junction instrument and removes the duplicate gated line. Scope stays inside github-page, docs/openspec and CHANGELOG.
