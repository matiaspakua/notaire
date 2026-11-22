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
| Issue | #2385 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-2385-harness-report-unknown-adapter/` | complete |
| Branch | `fix/2385_harness_report_unknown_adapter` | created |
| Tasks | `tasks.md` | 9/12 complete (groups 6-7, 11-12 n/a — kit-only) |
| Commits | 03665e55, 7c6919a7, 2046dfc8 | complete |
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
| an unknown adapter is refused | `test_harness_report.py::HarnessReportTest.test_harness_report_refuses_unknown_adapter` | passing |
| a known adapter still works | `test_harness_report.py::HarnessReportTest.test_harness_report_accepts_known_adapter` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/docs/HARNESS-REPORTING.md` | yes | 2046dfc8 |
| `framework/ai-sdlc/CHANGELOG.md` | yes | 2046dfc8 |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #2385, CU76, delta spec in `specs/harness-port/spec.md` with 2 scenarios |
| 2 | Failing tests written, test cases designed | yes | `test_harness_report_refuses_unknown_adapter` fails before fix (commit 03665e55), passes after (7c6919a7) |
| 3 | Suite green, coverage held, docs updated | yes | `env -u TRACEPARENT bash framework/ai-sdlc/verify.sh` passes; HARNESS-REPORTING.md and CHANGELOG.md updated (2046dfc8) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

- **`tests/test_runs.py` had to register the `local-worker` adapter.** Mechanical: the new validation rule now applies to existing tests that call `harness report`. The profile string in `test_runs.py` was updated to include `[[harness.adapters]]` so the test continues to pass.
- **Kit suite has one pre-existing failure (`test_boundaries`).** This failure already exists on `main`; it is caused by RepoLens naming the adopter and is fixed by PR #2383. It is unrelated to this change.
