# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> Rows below Tasks stay `pending` until the corresponding step actually happens.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1970 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/docs-1970-pages-ux-redesign/` | Gate 1 |
| Branch | `docs/1970_pages_ux_redesign` | from `origin/main` |
| Tasks | `tasks.md` | groups 1-9 done |
| Commits | see PR | pending |
| Pull Request | pending | pending |
| CI run | PR checks | pending |
| Merge commit | Owner merge | pending |
| Release / tag | next cut | pending |
| Smoke test | `/notaire/`, `/notaire/docs/`, `/notaire/ai-sdlc/` at 375 px after the Pages deploy | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Technical docs are one click away | `SiteHeader.test.tsx`; `e2e/site.spec.ts` | passing |
| Keyboard user opens and closes the menu | `SiteHeader.test.tsx`; `e2e/site.spec.ts` | passing |
| Search finds a page | `SearchDialog.test.tsx`; `e2e/site.spec.ts` | passing |
| Automated accessibility check | `e2e/site.spec.ts` | passing |
| A broken internal link | `scripts/check-links.mjs` in `pages-ci.yml` | passing |
| Visitor reaches the factory from both entry points | `test_story_nav_and_docs_tab_link_the_factory` | passing |
| Reader follows the story | `tests/site.test.ts`; `e2e/site.spec.ts` | passing |
| A count changes | `test_results_are_read_from_the_repository` | passing |
| Swimlane lanes follow the Constitution roles | `test_swimlane_lanes_match_the_constitution_roles` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | `[Unreleased]` Documentation | this PR |
| `CONSTITUTION.md` §13 | Public documentation site, Path-scoped CI, Last reviewed | this PR |
| `github-page/README.md` | shell, registry, tokens, diagrams, checks | this PR |
| `github-page/docs/ux-assessment.md` | new: P0/P1/P2 findings, after state | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1970, this change |
| 2 | Failing tests written, test cases designed | yes | test commit before implementation |
| 3 | Suite green, coverage held, docs updated | yes | vitest, tsc, eslint, build, check:links, Playwright + axe (27 passed), workspace tests, quick.sh |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Pages deploy |

## Exceptions

- Labels on #1970 could not be set: the token gets HTTP 403 on `addLabelsToLabelable`. Noted in the PR.
