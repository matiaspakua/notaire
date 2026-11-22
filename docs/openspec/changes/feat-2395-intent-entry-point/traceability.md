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
| Tasks | `tasks.md` | 12/13 complete |
| Commits | 13 commits (see below) | complete |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Commits

```
3376603d docs(aisdlc): keep the changelog diff to one line (#2395)
de83eecf docs(aisdlc): intents, MCP and changelog (#2395)
7db673e8 docs(aisdlc): contract 1.3.0 for intents and run binding (#2395)
ee9c586e fix(aisdlc): intent CLI errors go to stderr (#2395)
bb9e80cb fix(aisdlc): harden the MCP server and compact factory_status (#2395)
c1b07316 fix(aisdlc): validate intent ids and titles, honest source (#2395)
918b47e8 fix(aisdlc): run records its intent (#2395)
4a65cb83 feat(aisdlc): aisdlc mcp stdio server (#2395)
0235b1f2 feat(aisdlc): intents API and run binding (#2395)
ebfc0902 feat(aisdlc): aisdlc intent submit|list|show (#2395)
9785f6f4 feat(aisdlc): intent model service and spec parser (#2395)
b8694476 test(aisdlc): failing tests for the intent entry point (#2395)
4c395f38 docs(openspec): intent entry point change (#2395)
```

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| submit prompt intent | `IntentModelSubmitTest::test_submit_prompt_intent` | passing |
| submit reference intent without network calls | `IntentModelSubmitTest::test_submit_reference_intent` | passing |
| intent versioning and ledger privacy event | `IntentModelVersioningAndLedgerTest::test_intent_versioning_and_ledger` | passing |
| optional store mirror hook no-op | `IntentModelSubmitTest::test_store_mirror_noop` | passing |
| secrets in intent text are refused | `IntentModelSubmitTest::test_secrets_refused` | passing |
| unknown reference system is rejected | `IntentModelSubmitTest::test_unknown_reference_system` | passing |
| parse markdown spec into title, description, criteria, and typed references | `IntentModelSpecTest::test_parse_spec_markdown` | passing |
| spec file with missing title gets a derived title | `IntentModelSpecTest::test_spec_missing_title_derived` | passing |
| CLI submit text, ref, or file | `IntentCLITest::test_cli_submit_text`, `test_cli_submit_ref`, `test_cli_submit_file` | passing |
| CLI list intents | `IntentCLITest::test_cli_list` | passing |
| CLI show intent by id | `IntentCLITest::test_cli_show` | passing |
| GET /factory/intents returns intent list | `IntentServeTest::test_get_intents` | passing |
| GET /factory/intents/{id} returns intent details | `IntentServeTest::test_get_intent_by_id` | passing |
| POST /factory/intents creates intent with write scope, CSRF, and actor | `IntentServeTest::test_post_intent` | passing |
| POST /factory/runs accepts valid intent id and appends to run_ids | `IntentServeTest::test_post_runs_with_intent` | passing |
| POST /factory/runs rejects nonexistent intent id | `IntentServeTest::test_post_runs_unknown_intent` | passing |
| stdio MCP initialize and list tools | `MCPServerTest::test_mcp_initialize_and_list` | passing |
| MCP intent_submit tool | `MCPServerTest::test_mcp_intent_submit` | passing |
| MCP intent_show tool | `MCPServerTest::test_mcp_intent_show` | passing |
| MCP factory_status tool | `MCPServerTest::test_mcp_factory_status` | passing |
| same intent produced from CLI, API, and MCP | `IntentUniformRepresentationTest::test_uniform_representation` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | yes | de83eecf |
| `framework/ai-sdlc/CHANGELOG.md` | yes | de83eecf |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | yes | 7db673e8 |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2395, this folder, 21 scenarios |
| 2 | Failing tests written, test cases designed | yes | b8694476 — 56 tests, all observed failing before implementation |
| 3 | Suite green, coverage held, docs updated | yes | 545 passed, 4 skipped; ruff clean on new files; README, CHANGELOG, OpenAPI updated |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
