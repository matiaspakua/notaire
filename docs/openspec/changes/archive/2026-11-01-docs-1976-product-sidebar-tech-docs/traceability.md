# Traceability

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md) — P4 Traceability.
> Rows below Tasks stay `pending` until the corresponding step actually happens.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1976 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/docs-1976-product-sidebar-tech-docs/` | Gate 1 |
| Branch | `docs/1976_product_sidebar_tech_docs` | from `origin/main` |
| Tasks | `tasks.md` | groups 1-9 done |
| Commits | `8e557426c` (tests), `6d4070ab7` (pages), `d210c8d32` (docs), archive commit | done |
| Pull Request | pending | pending |
| CI run | PR checks | pending |
| Merge commit | Owner merge | pending |
| Release / tag | next cut | pending |
| Smoke test | `/notaire/product/`, `/notaire/docs/hexagonal/` at 375 px after the Pages deploy | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Reader browses the product pages | `tests/site.test.ts`; `e2e/site.spec.ts` product sidebar test; `test_pages_tech_docs.py` product layout | passing |
| Visitor lands on the home page | `tests/site.test.ts` (`sectionOf("/")`); `test_pages_tech_docs.py` landing doors | passing |
| Reader looks for how the database is versioned | `tests/site.test.ts` (search, Architecture internals group); `e2e/site.spec.ts` axe sweep | passing |
| A migration is added | `test_pages_tech_docs.py` (pages import `tech-facts`, no typed-in versions or counts) | passing |
| A cited file is renamed | `test_pages_tech_docs.py` (every `src()` path exists) | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/TECHNOLOGY-STACK.md` | new: stack and assessment | this PR |
| `docs/200-architecture/README.md` | links the stack page | this PR |
| `docs/200-architecture/201-SAD/sad.md` | v3.5: Spring Boot 4.1.2, V53 | this PR |
| `docs/200-architecture/209-deployment/README.md` | Java 26 | this PR |
| `README.md` | Spring Boot 4.1.2 | this PR |
| `CHANGELOG.md` | `[Unreleased]` Documentation | this PR |
| `CONSTITUTION.md` §13 | Public documentation site | this PR |
| `github-page/README.md` | routes | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1976, this change |
| 2 | Failing tests written, test cases designed | yes | test commit before implementation |
| 3 | Suite green, docs updated | yes | vitest, tsc, eslint, build, check:links, Playwright + axe, workspace tests, quick.sh |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Pages deploy |

## Exceptions

- Labels and milestone on #1976 could not be set: the token is refused. Noted in the PR.
