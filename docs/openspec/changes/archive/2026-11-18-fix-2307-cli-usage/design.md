> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The MCP server records every tool call. The CLI commands printed results and returned. `report.server_metrics` kept only `src=server` events.

## Goals / Non-Goals

**Goals:**

- A CLI search or impact creates one usage event.
- `repolens report` shows those calls.

**Non-Goals:**

- Changing search ranking.
- Rewriting the MCP server for mcp 2.

## Decisions

CLI events use `src=cli` and `client=cli`. The report treats them as calls next to server calls, so the gate line is no longer stuck at 0 when the only client is the CLI. The dependency pin stops a fresh install from selecting mcp 2.

## Riesgos / Trade-offs

- [A telemetry failure must not fail the query] → `record` already swallows IO errors.
- [mcp 2 is unsupported] → the extra is pinned below 2 until the server is ported.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Search and impact append two events and the report counts them | unit | `test_cli_search_is_counted_by_the_usage_report` |

TDD: the test raised `FileNotFoundError` on `usage.jsonl` before the CLI wrote it.

## Regression Strategy

- `bash framework/ai-sdlc/repolens/verify.sh`
- Full suite: `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no product UI.

- Specs to add/update under `testing/e2e/tests/`: none
- Command: `preflight.sh --full` still runs the suite

## Deployment Strategy

- Flyway: no
- Configuration: none
- Smoke: `verify.sh`

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
