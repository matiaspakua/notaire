> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Notaire runs Claude Code locally and in the cloud, Cursor cloud agents, and a local-AI worker on Codex/OpenCode, often several at once in separate worktrees (`notaire-wt-*`).

## Goals / Non-Goals

**Goals:** one-command setup, correct answers per branch under concurrency, measurable usage.
**Non-Goals:** changing the kit's code, enforcing tool choice on agents, remote telemetry.

## Decisions

1. **One store per worktree, one vector cache per machine.** Branches never mix; vectors are content-addressed so sharing them is always correct.
2. **Refresh in the background, answer from the last published graph.** Agents are never blocked by indexing; the lock makes concurrent refreshes step aside.
3. **Telemetry is local and stdlib-only.** The hook must never slow or fail a tool call; nothing leaves the machine.
4. **Advisory gate.** Usage numbers inform `improve`; they do not block delivery.

## Riesgos / Trade-offs

- [Owner-protected paths are touched] → the Owner merges (CONSTITUTION §5 step 21).
- [Hosts without the dependency stack] → MCP server unavailable, agents fall back to grep; the preflight gate skips with a reason; cloud bootstrap installs in the background.
- [Stale reads in long-lived servers] → fixed and guarded by the refresh test.

## Testing Strategy

`framework/ai-sdlc/repolens/tests/test_factory.py` covers every scenario; `workspace/tests/test_repolens_integration.py` guards the committed wiring.

## Regression Strategy

`bash framework/ai-sdlc/repolens/verify.sh`; `python3 -m unittest workspace.tests.test_repolens_integration`; `aisdlc gate signal`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
