> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The kit `framework/ai-sdlc/` already provides versioned-entity helpers in `runs.py`, `projects.py`, and `entities.py`. The existing `intents.py` handles knowledge-backend intent runs (`aisdlc intent start|end|tool|run`). Issue #2393 defines the shared Intent contract; #2395 implements the entry point.

## Goals / Non-Goals

**Goals:** one Intent model persisted as versioned JSON files; a single service module (`intent_model.py`) used by CLI, HTTP, and MCP; spec Markdown parsing; secrets rejection; run binding.
**Non-Goals:** UI intake form (child G); store mirror beyond best-effort hook (child A); external system calls for reference intents.

## Decisions

1. **`intent_model.py` as the sole service.** Functions `submit`, `list`, `show`, `attach_run` and the spec parser live here. CLI, serve, and MCP all call these functions — no duplicate logic.
2. **File-based versioned persistence.** Intents go to `.aisdlc/intents/<id>/current.json` and `versions/<n>.json`, reusing the same pattern as `runs.py` and `projects.py`.
3. **Spec Markdown parsing.** Title from `# Title` or `## Title`; description from body text; acceptance criteria from checkboxes under `## Acceptance criteria`; references from markdown links with kind inference.
4. **Secrets check.** Before persisting, scan intent text and spec content against `factory.SECRET_VALUES`; raise `KitError("literal_secret", …)` on match.
5. **Reference systems.** Only `github`, `jira`, `url`, `file` are accepted; anything else raises `KitError`.
6. **Missing title.** If a spec file has no heading, derive the title from the first non-empty description line.
7. **MCP stdlib-only.** The MCP server uses `json` and `sys.stdin`/`sys.stdout` for JSON-RPC 2.0 over stdio — no third-party packages.

## Riesgos / Trade-offs

- [File-based persistence is not concurrent] → the kit is single-process per workspace; concurrent writes are not expected.
- [Secrets regex may produce false positives] → `SECRET_VALUES` is conservative (known prefixes like `ghp_`, `sk-`, `AKIA`); false positives are acceptable as they block accidental secret leaks.
- [Parallel workers editing cli.py/serve.py] → keep additions in separate functions/blocks; do not reformat existing code.

## Testing Strategy

Every `#### Scenario:` in the delta spec maps to at least one test.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| submit prompt intent | unit | `test_intent_model.py::test_submit_prompt_intent` |
| submit reference intent without network calls | unit | `test_intent_model.py::test_submit_reference_intent` |
| intent versioning and ledger privacy event | unit | `test_intent_model.py::test_intent_versioning_and_ledger` |
| optional store mirror hook no-op | unit | `test_intent_model.py::test_store_mirror_noop` |
| secrets in intent text are refused | unit | `test_intent_model.py::test_secrets_refused` |
| unknown reference system is rejected | unit | `test_intent_model.py::test_unknown_reference_system` |
| parse markdown spec into title, description, criteria, and typed references | unit | `test_intent_model.py::test_parse_spec_markdown` |
| spec file with missing title gets a derived title | unit | `test_intent_model.py::test_spec_missing_title_derived` |
| CLI submit text, ref, or file | integration | `test_intent_cli.py::test_cli_submit_text`, `test_cli_submit_ref`, `test_cli_submit_file` |
| CLI list intents | integration | `test_intent_cli.py::test_cli_list` |
| CLI show intent by id | integration | `test_intent_cli.py::test_cli_show` |
| GET /factory/intents returns intent list | integration | `test_intent_serve.py::test_get_intents` |
| GET /factory/intents/{id} returns intent details | integration | `test_intent_serve.py::test_get_intent_by_id` |
| POST /factory/intents creates intent with write scope, CSRF, and actor | integration | `test_intent_serve.py::test_post_intent` |
| POST /factory/runs accepts valid intent id and appends to run_ids | integration | `test_intent_serve.py::test_post_runs_with_intent` |
| POST /factory/runs rejects nonexistent intent id | integration | `test_intent_serve.py::test_post_runs_unknown_intent` |
| stdio MCP initialize and list tools | integration | `test_mcp_server.py::test_mcp_initialize_and_list` |
| MCP intent_submit tool | integration | `test_mcp_server.py::test_mcp_intent_submit` |
| MCP intent_show tool | integration | `test_mcp_server.py::test_mcp_intent_show` |
| MCP factory_status tool | integration | `test_mcp_server.py::test_mcp_factory_status` |
| same intent produced from CLI, API, and MCP | integration | `test_intent_model.py::test_uniform_representation` |

- New unit tests: `framework/ai-sdlc/tests/test_intent_model.py`
- New integration tests: `framework/ai-sdlc/tests/test_intent_cli.py`, `framework/ai-sdlc/tests/test_intent_serve.py`, `framework/ai-sdlc/tests/test_mcp_server.py`
- Coverage impact: new modules covered from day one; existing `intents.py` tests remain green.
- Command: `cd framework/ai-sdlc/tests && python3 -m pytest -q`

## Regression Strategy

- Existing tests affected: `test_intents.py` (knowledge-backend intent runs) — must remain green; no changes to existing subcommands.
- Full suite command: `cd framework/ai-sdlc/tests && python3 -m pytest -q`
- HTTP/Bruno API suite: no backend-api change; Bruno tests unaffected.
- Legacy paths at risk: none — the kit is Python-only.

## Playwright Strategy

n/a — no UI surface. This change adds CLI, HTTP, and MCP entry points only; the dashboard intake form is out of scope (child G).

## Deployment Strategy

- Flyway migration required: no — file-based persistence only.
- Deployment order / coupling: ships with the next kit release; additive endpoints.
- Configuration or `.env` keys to add: none.
- Feature flag: no.
- Smoke test after deploy (Gate 5): `aisdlc intent submit --text "smoke"` then `aisdlc intent list --json`.

## Rollback Strategy

- Revert safe: yes — additive endpoints and new files; reverting removes them without affecting existing functionality.
- Database rollback: none needed.
- Data written under the new behavior after revert: intent JSON files under `.aisdlc/intents/` remain; harmless orphan data.
- Blast radius if rollback is delayed: none — existing intent subcommands (`start`, `end`, `tool`, `run`) are untouched.

## Migration Plan

None — no data migration required.

## Open Questions

None.
