# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #921 | open — this change is one slice; the issue stays open |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related use cases | CU77, CU83, CU02, CU16, CU85, CU06, CU10, CU72, CU15, CU47 | exists; cited, not rewritten |
| Specification | `docs/openspec/changes/docs-921-domain-lifecycle/` | Gate 1 passed locally |
| Branch | `cursor/lifecycle-engineering-docs-853b` | created |
| Tasks | `tasks.md` | in progress (merge and review still open) |
| Commits | lifecycle docs, then this plan and the process-check fix | on the branch |
| Pull Request | #1492 | open |
| CI run | Process Checks job 114337168011 failed on `9e039446` (no change folder); job 114337749465 passed on `bb74efb4` only because main's `fix-1340-deed-pickers` was inside the stale-base diff | corrected in this change |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | Process Checks on the corrected diff | pending |

## Requirement coverage

`skip_specs: true` — no new product requirement. The checks that prove this change:

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| A merge checkout ignores an OpenSpec path that landed on main | `workspace/tests/test_pr_checks.py` | written, was failing before the script fix |
| A merge checkout accepts an OpenSpec path the PR added | `workspace/tests/test_pr_checks.py` | written |
| Permanent doc links resolve | `docs/tests/test_docs_links.py` | passing |
| Requirement and use-case ids still cross-check | `docs/tests/test_business_docs_traceability.py` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/203-design/CICLO-DE-VIDA.md` | yes | on this branch |
| `docs/300-development/INGENIERIA.md` | yes | on this branch |
| SAD, ADR-014, indexes | yes | on this branch |
| `CHANGELOG.md` | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue #921 open, plan complete, `skip_specs` justified | yes | `bash workspace/sdlc/validate-sdlc-plan.sh docs-921-domain-lifecycle` |
| 2 | Failing test observed, then the script fixed | yes | `test_merge_checkout_ignores_openspec_files_that_landed_on_main` |
| 3 | Docs consistent, changelog noted | yes | this change |
| 4 | CI green and review approved | pending | pull request #1492 |
| 5 | Merged; #921 still open | pending | — |

## Exceptions

None. The `sdlc-exception` label is not requested. This folder is the change record.
