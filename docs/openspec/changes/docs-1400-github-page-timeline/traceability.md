# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1400 | open (agent cannot add `in-progress` — label API 403) |
| Probe to close | #1399 (`probe`) | open — admin close |
| Use Case | CU76 | linked |
| Specification | `docs/openspec/changes/docs-1400-github-page-timeline/` | Gate 1 draft |
| Branch | `cursor/docs-1400-github-page-timeline-c19f` | created |
| Tasks | `tasks.md` | in progress |
| Pull Request | [#1402](https://github.com/matiaspakua/notaire/pull/1402) | draft |
| Skip | #1197 | per Owner |

## Requirement coverage

| Scenario (Acceptance Criterion) | Verification | Status |
|---------------------------------|--------------|--------|
| Prior timeline eras remain | Diff review of `Timeline.tsx` events[0..n-1] | pending |
| Sept–Oct 2026 era present | New event(s) in `Timeline.tsx` | pending |
| Deploy workflow untouched | `git diff` on `deploy-github-page.yml` empty | pending |
| English touched copy | Review | pending |
| `github-page` builds | `npm run build` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | pending | this PR |
| Sanity store report | store `docs/sanity-github-project-pages.md` | n/a (store) |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + AC | pending | this change + #1400 |
| 2 | Tests / verification designed | pending | build + diff review |
| 3 | Suite green / docs updated | pending | |
| 4 | CI / draft PR | pending | |
| 5 | Deployed / Issue closed | pending | human merge + close |

## Exceptions

- Issue label `in-progress` and close operations: integration token returns 403 (`addLabelsToLabelable` / `closeIssue`). Admin applies labels and closes #1400 / #1399.
- Product Playwright / Bruno suites: waived for this docs-only Pages copy change (CONSTITUTION docs path; no product UI).
