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
| Issue | #2395 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2395-intent-entry-point/` | passed |
| Branch | `feat/2395_intent_entry_point` | created |
| Tasks | `tasks.md` | pending |
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
| submit prompt intent | `test_intent_model.py::test_submit_prompt_intent` | pending |
| submit reference intent without network calls | `test_intent_model.py::test_submit_reference_intent` | pending |
| intent versioning and ledger privacy event | `test_intent_model.py::test_intent_versioning_and_ledger` | pending |
| optional store mirror hook no-op | `test_intent_model.py::test_store_mirror_noop` | pending |
| secrets in intent text are refused | `test_intent_model.py::test_secrets_refused` | pending |
| unknown reference system is rejected | `test_intent_model.py::test_unknown_reference_system` | pending |
| parse markdown spec into title, description, criteria, and typed references | `test_intent_model.py::test_parse_spec_markdown` | pending |
| spec file with missing title gets a derived title | `test_intent_model.py::test_spec_missing_title_derived` | pending |
| CLI submit text, ref, or file | `test_intent_cli.py::test_cli_submit_text`, `test_cli_submit_ref`, `test_cli_submit_file` | pending |
| CLI list intents | `test_intent_cli.py::test_cli_list` | pending |
| CLI show intent by id | `test_intent_cli.py::test_cli_show` | pending |
| GET /factory/intents returns intent list | `test_intent_serve.py::test_get_intents` | pending |
| GET /factory/intents/{id} returns intent details | `test_intent_serve.py::test_get_intent_by_id` | pending |
| POST /factory/intents creates intent with write scope, CSRF, and actor | `test_intent_serve.py::test_post_intent` | pending |
| POST /factory/runs accepts valid intent id and appends to run_ids | `test_intent_serve.py::test_post_runs_with_intent` | pending |
| POST /factory/runs rejects nonexistent intent id | `test_intent_serve.py::test_post_runs_unknown_intent` | pending |
| stdio MCP initialize and list tools | `test_mcp_server.py::test_mcp_initialize_and_list` | pending |
| MCP intent_submit tool | `test_mcp_server.py::test_mcp_intent_submit` | pending |
| MCP intent_show tool | `test_mcp_server.py::test_mcp_intent_show` | pending |
| MCP factory_status tool | `test_mcp_server.py::test_mcp_factory_status` | pending |
| same intent produced from CLI, API, and MCP | `test_intent_model.py::test_uniform_representation` | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | no | pending |
| `framework/ai-sdlc/CHANGELOG.md` | no | pending |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2395, this folder, 21 scenarios |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
