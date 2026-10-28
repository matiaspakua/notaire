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
| ------ | ----------- | -------- |
| Issue | #1958 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-1958-generic-ai-sdlc-framework/` | passed |
| Branch | `chore/1958_generic_ai_sdlc_framework` | pushed |
| Tasks | `tasks.md` | groups 1–9 done; 10–12 at PR, merge and deploy |
| Commits | b4d37c4b, b14ceef7, f8b7e36b, eb56431b, b26d32ca, e9f6acf1, e1a2723d, 872f58dd, 24d91550, 7b19dcd1, c308f66a | pushed with the PR |
| Pull Request | recorded at merge | pending |
| CI run | recorded at merge | pending |
| Merge commit | recorded at merge (Owner merge: harness and Constitution change) | pending |
| Release / tag | next release cut | pending |
| Smoke test | Pages deploy shows `/ai-sdlc/case-study/` and `/ai-sdlc/framework/` | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
| --------------------------------- | ------ | -------- |
| a required gate fails | `framework/ai-sdlc/tests/test_gates.py` · `test_required_failing_gate_fails_the_stage_and_is_named` | passing |
| a red gate expects failure | `framework/ai-sdlc/tests/test_gates.py` · `test_red_gate_passes_when_its_command_fails` | passing |
| a path-scoped gate with no matching change | `framework/ai-sdlc/tests/test_gates.py` · `test_path_scoped_gate_is_skipped_without_matching_change` | passing |
| ledger after a stage run | `framework/ai-sdlc/tests/test_evidence.py` · `test_stage_run_appends_one_record_with_every_gate` | passing |
| report over the ledger | `framework/ai-sdlc/tests/test_evidence.py` · `test_report_shows_runs_and_pass_rate_per_gate` | passing |
| a hand-added baseline line | `framework/ai-sdlc/tests/test_ratchet.py` · `test_hand_added_line_fails_and_is_printed` | passing |
| a shrunk baseline | `framework/ai-sdlc/tests/test_ratchet.py` · `test_shrunk_baseline_passes` | passing |
| a JSON suppression count grows | `framework/ai-sdlc/tests/test_ratchet.py` · `test_growing_count_fails` | passing |
| a coverage floor is missed | `framework/ai-sdlc/tests/test_ratchet.py` · `test_value_below_floor_fails` | passing |
| duplicate skill names | `framework/ai-sdlc/tests/test_skills.py` · `test_duplicate_names_fail_naming_both_folders` | passing |
| a project path in a generic skill | `framework/ai-sdlc/tests/test_skills.py` · `test_project_path_in_generic_skill_fails_naming_the_file` | passing |
| over budget | `framework/ai-sdlc/tests/test_checks.py` · `test_over_budget_fails_and_prints_the_total` | passing |
| within budget | `framework/ai-sdlc/tests/test_checks.py` · `test_within_budget_passes` | passing |
| non-conventional commit | `framework/ai-sdlc/tests/test_checks.py` · `test_non_conventional_subject_fails` | passing |
| production change without tests | `framework/ai-sdlc/tests/test_checks.py` · `test_production_change_without_tests_fails` | passing |
| unfilled section | `framework/ai-sdlc/tests/test_change.py` · `test_unfilled_section_fails_naming_it` | passing |
| invented issue number | `framework/ai-sdlc/tests/test_change.py` · `test_invented_issue_number_fails` | passing |
| stack-independent run | `framework/ai-sdlc/tests/test_init_doctor.py` · `test_fresh_repository_passes_doctor_and_pre_push_with_posix_tools_only` | passing |
| no adopter path in the kit | `framework/ai-sdlc/tests/test_independence.py` · `test_no_adopter_path_in_code_or_templates` | passing |
| Notaire profile is valid | `workspace/tests/test_aisdlc_adoption.py` · `test_profile_passes_doctor` | passing |
| Process Checks run the ratchet | `workspace/tests/test_aisdlc_adoption.py` · `test_process_checks_run_ratchet_skills_and_kit_tests`; live check: a hand-added hexagon baseline line fails `aisdlc ratchet --base origin/main` | passing |
| the case study page exists and is linked | `workspace/tests/test_pages_ai_sdlc.py` · `test_every_subpage_exists`; static build lists `/ai-sdlc/case-study` | passing |
| history comes from a snapshot with provenance | `workspace/tests/test_pages_ai_sdlc.py` · `test_history_snapshot_records_its_provenance`; `workspace/tests/test_generate_ai_sdlc_history.py` | passing |
| the framework page cites the kit | `workspace/tests/test_pages_ai_sdlc.py` · `test_framework_page_cites_the_kit_and_the_report`, `test_every_cited_path_exists` | passing |
| animation respects reduced motion | `workspace/tests/test_pages_ai_sdlc.py` · `test_gate_flow_animation_respects_reduced_motion`; Playwright screenshot with `reducedMotion: reduce` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
| ---------- | --------- | -------- |
| `docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md` | yes | 872f58dd |
| `framework/ai-sdlc/README.md` | yes | f8b7e36b |
| `CONSTITUTION.md` | yes | c308f66a |
| `.claude/skills/README.md` | yes | eb56431b |
| `github-page/README.md` | yes | 7b19dcd1 |
| `docs/300-development/README.md` | yes | 7b19dcd1 |
| `CHANGELOG.md` | yes | 7b19dcd1 |

## Gate log

| Gate | Condition | Passed | Evidence |
| ------ | ----------- | -------- | ---------- |
| 1 | Issue + Specification + Acceptance Criteria | yes | #1958, this folder |
| 2 | Failing tests written, test cases designed | yes | b14ceef7 (kit, adoption, Pages red), e9f6acf1 (hook env red) |
| 3 | Suite green, coverage held, docs updated | yes | process self-tests green per module; `github-page` static build; preflight on push |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
