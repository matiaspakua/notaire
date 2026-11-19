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
| Issue | #2338 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2338-repolens-agnostic-knowledge-orphans/` | passed |
| Branch | `feat/2338_repolens-agnostic-orphans` | created |
| Tasks | `tasks.md` | groups 1-9 done |
| Commits | `e1f3411c`..archive commit | done |
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
| a language without a hand-written spec | `test_a_tags_query_yields_definitions_and_calls`, `test_detection_reaches_languages_without_a_hand_written_spec` | passing |
| a grammar without a tags query | `test_a_grammar_without_a_tags_query_still_yields_functions`, `test_an_unavailable_grammar_falls_back_to_text_once` | passing |
| a format with no definitions | `test_detection_reaches_languages_without_a_hand_written_spec` (Terraform, Groovy as text) | passing |
| paths in any form | `test_paths_in_any_form_are_linked` | passing |
| an ambiguous bare name | `test_an_ambiguous_bare_name_links_the_nearest_or_nothing` | passing |
| identifiers in configuration | `test_identifiers_in_configuration_link_the_symbol` | passing |
| an isolated file | `test_files_related_to_nothing_and_only_through_history`, `test_symbols_with_no_relation_on_request` | passing |
| related only through history | `test_files_related_to_nothing_and_only_through_history` | passing |
| every interface | `test_http_and_mcp_return_the_same_files`, `test_the_cli_lists_orphans_and_the_index_run_alerts` | passing |
| the index run alerts | `test_the_cli_lists_orphans_and_the_index_run_alerts` | passing |
| an ESM TypeScript import | `test_an_esm_import_of_the_js_file_resolves_to_the_ts_source` | passing |
| a file a tool reads by name | `test_files_tools_read_by_name_are_listed_on_request_or_without_the_convention`, `test_demoted_paths_are_listed_on_request` | passing |
| a named directory relates its files | `test_a_named_directory_relates_its_files` | passing |
| measured | `bench/orphan_bench.py` -> `bench/results/orphans-{0.5.0,0.6.0}-{notaire,scale}.json`; README tables | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/repolens/README.md` | yes | `226d0900` |
| `framework/ai-sdlc/repolens/CHANGELOG.md` | yes | `226d0900` |
| `framework/ai-sdlc/repolens/bench/results/factory-kpis.json` | yes | `226d0900` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2338, this folder |
| 2 | Failing tests written, test cases designed | yes | each `test(repolens)` commit precedes its `feat`/`fix` commit and failed on the code before it |
| 3 | Suite green, coverage held, docs updated | yes | `verify.sh`: 126 passed, coverage 89.5% (floor 88), wheel and smoke OK; markdownlint clean |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
