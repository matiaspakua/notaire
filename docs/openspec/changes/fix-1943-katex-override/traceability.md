# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> Rows below Tasks stay `pending` until the corresponding step actually happens.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1943 | in-progress |
| Use Case | CU78 — Security | exists |
| Specification | `docs/openspec/changes/fix-1943-katex-override/` | Gate 1 |
| Branch | `fix/1943_katex_override` | from `origin/main` |
| Tasks | `tasks.md` | groups 1–9 done; 10–12 at PR/merge |
| Commits | see PR | recorded in PR |
| Pull Request | pending | pending |
| CI run | PR checks | pending |
| Merge commit | Owner merge | pending |
| Release / tag | next cut | pending |
| Smoke test | Dependabot katex alert closed on `main` | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| package.json declares a katex override | `test_package_json_declares_patched_katex_override` | passing |
| lockfile resolves katex to a patched version | `test_lockfile_resolves_patched_katex` | passing |
| markdownlint-cli2 still lints the repository Markdown | `test_markdownlint_cli2_runs_on_math_markdown` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | this PR |
| `docs/openspec/specs/frontend-katex-override/spec.md` | yes | archive commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1943, this change |
| 2 | Failing tests written, test cases designed | yes | test commit before implementation |
| 3 | Suite green, coverage held, docs updated | yes | quick.sh |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Dependabot alert closed |

## Exceptions

None.
