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
| Issue | #2203 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-2203-pages-metrics-no-fake-zero/` | complete |
| Branch | `cursor/fix-2203-pages-metrics-no-fake-zero-fc7d` | created |
| Tasks | `tasks.md` | 0/N complete |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Missing snapshot and missing live do not publish zero coverage | `workspace/tests/test_generate_github_page_metrics.py` | pending |
| Poisoned live zeros with unknown stamp are not copied forward | `workspace/tests/test_generate_github_page_metrics.py` | pending |
| Present JaCoCo snapshot updates backend fields | `workspace/tests/test_generate_github_page_metrics.py` | pending |
| Present frontend summary updates frontendComponent | `workspace/tests/test_generate_github_page_metrics.py` | pending |
| Measured live fallback reused when snapshot missing | `workspace/tests/test_generate_github_page_metrics.py` | pending |
| UI treats null / unknown as not measured | `github-page/tests/metrics-coverage-honesty.test.ts` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | no | |
| `github-page/README.md` | no | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh` exit 0 |
| 2 | Failing tests written, test cases designed | yes | tdd-red.txt then green unittest |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None.
