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
| Issue | #2469 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-2469-factory-evidence-refresh/` | passed |
| Branch | `chore/2469_factory_evidence_refresh` | created |
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
| a retrieval run is stamped | `framework/repolens/tests/test_bench_meta.py::test_retrieval_results_are_stamped_with_the_repolens_version` | pending |
| the default refresh is free | `test_refresh_factory_evidence.py::test_default_steps_are_free_and_ordered` | pending |
| agent benches are opt-in | `test_refresh_factory_evidence.py::test_agent_and_history_are_opt_in` | pending |
| results from an older RepoLens are stale | `test_refresh_factory_evidence.py::test_result_from_another_version_is_stale` | pending |
| an engine behind origin/main is stale | `test_refresh_factory_evidence.py::test_engine_behind_origin_main_is_stale` | pending |
| a new latency factor validates | `workspace/tests/test_generate_repolens_bench.py::test_validate_accepts_any_well_formed_headline_label` | passing |
| a malformed headline is rejected | `test_generate_repolens_bench.py::test_validate_rejects_a_malformed_headline_label` | passing |
| the tuning set keeps its chart names | `framework/repolens/tests/test_bench_meta.py::test_a_task_set_chart_tag_is_carried_into_the_result` | passing |
| (session start) | `workspace/tests/test_refresh_factory_evidence.py::SessionStartTest` | passing |
| everything current | `test_refresh_factory_evidence.py::test_current_engines_and_results_pass` | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/repolens/bench/README.md` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-METRICS.md` | no | pending |
| `.claude/rules/hooks.md` | no | pending |
| `AGENTS.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2469, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
