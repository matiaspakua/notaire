> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`setup.py` already provides `setup_cursor` which writes `.cursor/mcp.json` with shape `{"mcpServers": {"repolens": {"command": ..., "args": [...]}}}`. Junie uses the identical shape at `.junie/mcp/mcp.json`. The pattern is mechanical: add a writer, register it, add a probe.

## Goals / Non-Goals

**Goals:**
- `repolens setup` writes `.junie/mcp/mcp.json` with the same launcher as every other harness.
- `--dry-run` reports Junie drift.
- Committed `.junie/mcp/mcp.json` matches the generator.
- Integration test asserts Junie server equals all others.

**Non-Goals:**
- Junie-specific permissions, hooks, or tool configuration beyond server registration.

## Decisions

- **Follow Cursor pattern exactly.** Junie's MCP config shape (`{"mcpServers": {"repolens": {"command": ..., "args": [...]}}}`) is identical to Cursor's. The writer mirrors `setup_cursor` with only the path changed. No `"type"` key is needed (Cursor doesn't use one; Junie doesn't either).
- **Add `"junie"` to TARGETS tuple.** This makes `setup()` and `--dry-run` cover Junie by default, consistent with how cursor/git/gitignore are treated.

## Riesgos / Trade-offs

No risk. This is a mechanical extension of an existing pattern. If `.junie/mcp/mcp.json` is accidentally not committed, the integration test catches it.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Setup writes `.junie/mcp/mcp.json` with same launcher as cursor | Unit | `framework/repolens/tests/test_factory.py` |
| `--dry-run` reports Junie drift | Unit | `framework/repolens/tests/test_factory.py` |
| Junie starts same server as every other harness | Integration | `workspace/tests/test_repolens_integration.py` |

- New unit tests: `test_factory.py` — extend existing `test_setup_registers_every_harness_once_and_keeps_existing_entries` to assert Junie entry.
- New integration test: `test_repolens_integration.py` — add Junie to `test_every_harness_starts_the_same_server`.

## Regression Strategy

- Existing tests affected: `test_factory.py::test_setup_registers_every_harness_once_and_keeps_existing_entries` (now checks Junie too), `test_repolens_integration.py::test_every_harness_starts_the_same_server` (adds Junie assertion).
- Full suite command: `$HOME/.cache/repolens/venv/bin/python -m pytest -q -p no:cacheprovider framework/repolens/tests/`
- No Java, no UI, no database.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — pure Python + config file
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `repolens setup --dry-run` shows no Junie drift; `repolens doctor` shows `junie: registered`

## Rollback Strategy

- Revert safe: yes — removing the commit restores the previous TARGETS tuple and deletes `.junie/mcp/mcp.json`
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none — Junie simply won't have RepoLens until the commit lands

## Open Questions

None.
