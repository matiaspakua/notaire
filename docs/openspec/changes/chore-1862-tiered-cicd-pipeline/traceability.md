# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1862 | in-progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-1862-tiered-cicd-pipeline/` | Gate 1 planned |
| Branch | `chore/1862_tiered_cicd_run_pipeline_sonarqube` | current |
| Tasks | `tasks.md` | 0/31 complete |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| `--with-infra` makes SonarQube blocking | `workspace/tests/test_run_pipeline_invariants.py` | pending |
| `--require-sonar` fails when SonarQube is unavailable | `workspace/tests/test_run_pipeline_invariants.py` | pending |
| `--fast` avoids server-backed phases | `workspace/tests/test_run_pipeline_invariants.py` | pending |
| Conflicting Sonar flags fail before running gates | `workspace/tests/test_run_pipeline_invariants.py` | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/PROJECT-CICD-PIPELINE-IMPROVEMENT.md` | pending | pending |
| `CHANGELOG.md` | n/a — no user-visible release change | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | passed | `openspec validate --strict` and `validate-sdlc-plan.sh` pending final plan check |
| 2 | Failing tests written, test cases designed | pending | CLI contract tests planned |
| 3 | Suite green, coverage held, docs updated | pending | pipeline evidence pending |
| 4 | CI green, review approved, no conflicts | pending | PR pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | not applicable until merge and release |

## Exceptions

No exception requested or taken.
