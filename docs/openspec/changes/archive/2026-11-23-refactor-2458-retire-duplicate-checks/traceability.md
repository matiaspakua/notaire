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
| Issue | #2458 (epic #2456) | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/refactor-2458-retire-duplicate-checks/` | passed |
| Branch | `refactor/2458_retire_duplicate_checks` | created |
| Tasks | `tasks.md` | in progress |
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
| production code committed before its test | `framework/ai-sdlc/tests/test_checks.py::TddEvidenceTest::test_order_fails_on_code_before_tests` | pending |
| the test is committed first | `test_checks.py::TddEvidenceTest::test_order_passes_tests_first` | pending |
| order is not configured | `test_checks.py::TddEvidenceTest::test_without_order_only_presence_counts` | pending |
| the exception label waives the check | `test_checks.py::TddEvidenceTest::test_waive_label_passes` | pending |
| another label does not waive it | `test_checks.py::TddEvidenceTest::test_unlisted_label_does_not_waive` | pending |
| docs/tests counts for TDD evidence | `workspace/tests/test_pr_checks.py::TddEvidenceTest::test_accepts_docs_tests_as_tdd_evidence` | pending |
| Constitution names self-improvement | `workspace/tests/test_governance_principles.py` (unchanged) | passing |
| Every Dependabot prefix is an allowed type | `security/tests/test_image_pins_and_dependabot.py::test_dependabot_commit_prefixes_are_allowed_conventional_types` | pending |
| Notaire's commit forms pass | `workspace/tests/test_pr_checks.py::CommitMessagesTest` | pending |
| Notaire's TDD rules hold | `workspace/tests/test_pr_checks.py::TddEvidenceTest` | pending |
| the duplicate scripts are gone | `workspace/tests/test_scripts_layout.py::test_removed_scripts_are_gone` | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `CONSTITUTION.md` | no | pending |
| `AGENTS.md` | no | pending |
| `docs/300-development/303-testing/PROCESS-ARCHITECTURE.md` | no | pending |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md` | no | pending |
| `docs/200-architecture/202-ADR/ADR-024-repository-topology.md` | no | pending |
| `framework/ai-sdlc/README.md` | no | pending |
| `framework/ai-sdlc/CHANGELOG.md` | no | pending |
| `github-page/lib/ai-sdlc-content.ts` | no | pending |
| `github-page/lib/factory-line-content.ts` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2458, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
