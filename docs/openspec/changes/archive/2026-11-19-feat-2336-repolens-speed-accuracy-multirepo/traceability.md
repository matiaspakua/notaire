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
| Issue | #2336 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2336-repolens-speed-accuracy-multirepo/` | passed |
| Branch | `feat/2336_repolens-speed-callers-multirepo` | created |
| Tasks | `tasks.md` | groups 1-9 done |
| Commits | `7d4cbf1b`..archive commit, see Gate log | done |
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
| same-named methods on several classes | `test_the_receiver_type_picks_the_method_among_same_named_ones` | passing |
| chains, generics, lambdas and method references | `test_chained_calls_follow_return_types`, `test_var_takes_the_type_of_its_initializer`, `test_generic_elements_lambdas_and_method_references`, `test_implicit_this_static_and_interface_calls` | passing |
| a library method is not linked | `test_a_library_method_is_not_linked_to_a_same_named_repository_method`, `test_spring_data_methods_return_the_repository_entity` | passing |
| measured against the compiler | `bench/caller_bench.py`: P 0.714 -> 0.989, R 0.852 -> 0.954 (`bench/results/callers-0.5.0.json`) | passing |
| callers through a port | `test_impact_of_an_implementation_includes_callers_through_its_interface` | passing |
| the second query is served by the service | `test_the_second_query_is_served_by_the_service` | passing |
| the service follows HEAD and stops when idle | `test_the_service_follows_head`, `test_an_idle_service_exits`, `test_an_outdated_service_is_replaced` | passing |
| disabled | `test_disabled_runs_in_process_and_starts_nothing` | passing |
| a new machine imports the cache | `test_a_new_machine_imports_the_cache_and_embeds_nothing`, `test_import_refuses_a_cache_with_another_basis` | passing |
| skipped paths | `test_skipped_paths_have_no_vector_but_keyword_search_finds_them` | passing |
| registry | `test_every_index_run_registers_its_store`, `test_the_registry_drops_stores_that_no_longer_exist` | passing |
| federated search | `test_a_federated_search_merges_and_labels_repositories`, `test_mcp_search_accepts_repositories`, `test_a_repository_reindexed_elsewhere_is_searched_fresh` | passing |
| schema budget | `test_tool_schemas_stay_within_the_byte_budget` | passing |
| the repository that matches the question ranks first | `test_a_repository_that_matches_the_question_better_ranks_first`, `test_equally_relevant_repositories_interleave`, `test_relevance_from_different_embedding_spaces_is_not_compared`, `test_a_store_reports_its_relevance_and_embedding_space`, `test_the_service_answers_a_federated_request_with_hits_and_relevance` | passing |
| a repository out of reach is not searched | `test_a_repository_too_far_behind_to_place_a_hit_is_not_searched` | passing |
| federated recall | `bench/federation_bench.py`: 0.786 / 0.825 own = weighted (`bench/results/federation-0.5.0.json`) | passing |
| an unknown name | `test_an_unknown_name_is_not_answered_with_a_guess`, `test_navigation_misses_name_the_closest_symbols` | passing |
| impact and node | `test_impact_lists_each_file_once_with_its_symbols`, `test_impact_marks_what_lies_in_demoted_paths`, `test_node_lists_related_symbols_by_file_without_repeating_paths` | passing |
| one copy per answer | `test_a_tool_answer_is_sent_once`, `test_tool_schemas_stay_within_the_byte_budget` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/repolens/README.md` | yes | `2c8a74e3` |
| `framework/ai-sdlc/repolens/bench/results/factory-kpis.json` | yes | `2c8a74e3` |
| `framework/ai-sdlc/repolens/CHANGELOG.md` | yes | `2c8a74e3` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2336, this folder |
| 2 | Failing tests written, test cases designed | yes | each `test(repolens)` commit precedes its `feat`/`fix` commit and failed on the code before it |
| 3 | Suite green, coverage held, docs updated | yes | `verify.sh`: 110 passed, coverage 89% (floor 88), wheel and smoke OK; markdownlint clean |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
