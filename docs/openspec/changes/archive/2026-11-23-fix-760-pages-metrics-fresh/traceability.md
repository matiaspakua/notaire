# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #760 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-760-pages-metrics-fresh/` | archived |
| Branch | `fix/760_pages_metrics_fresh` | created |
| Tasks | `tasks.md` | 4/12 complete (Gates 1-3 partial; 4-12 pending) |
| Commits | 7608c12f, 6b8e3014, cef0054b, b7e429e0 | done |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Fresh e2eTests from Playwright results | `test_generate_github_page_metrics.py::TestFreshCoverage` | passing |
| Fresh apiEndpoints from Bruno report | `test_generate_github_page_metrics.py::TestFreshCoverage` | passing |
| Stale fallback when e2e computation fails | `test_generate_github_page_metrics.py::TestStaleFallback` | passing |
| Stale fallback when API computation fails | `test_generate_github_page_metrics.py::TestStaleFallback` | passing |
| `--json` flag outputs correct counts | `test_generate_e2e_coverage_report.py` | n/a — script parses JSON directly in shell |
| `--json` flag handles missing artifacts | `test_generate_e2e_coverage_report.py` | n/a — script parses JSON directly in shell |
| UI renders stale badge for fallback values | Coverage.tsx (manual inspection) | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | no — not user-visible (internal metrics page) | n/a |
| `generate-github-page-metrics.sh` inline comments | yes | cef0054b |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #760 + spec in `specs/github-page-metrics-coverage-honesty/` |
| 2 | Failing tests written, test cases designed | yes | 6b8e3014 — 8 failing tests added |
| 3 | Suite green, coverage held, docs updated | partial | Implementation complete; full suite not yet run |
| 4 | CI green, review approved, no conflicts | pending | PR not yet created |
| 5 | Deployed, smoke test passed, Issue closed | pending | Not yet merged |

## Exceptions

None
