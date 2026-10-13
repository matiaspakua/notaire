> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

See the proposal. OpenCode starts MCP servers from the project root, so `.` resolves to the checkout.

## Goals / Non-Goals

**Goals:** any machine can run OpenCode; lost work is visible; pushes are verified.
**Non-Goals:** changing RTK; blocking session start.

## Decisions

1. **Upstream as the "was pushed" signal.** Squash merges delete remote branches, so "commits on no remote" alone lists every merged branch. In this checkout that dropped the list from 10 false positives to 1 real branch.
2. **Skills on demand for OpenCode too**, matching the Claude Code budget (#1259).

## Riesgos / Trade-offs

- [A branch pushed and then reset locally with new commits] → it has an upstream, so it is not listed. Accepted: rare, and `git status` shows it.

## Testing Strategy

`workspace/tests/test_harness_portability.py` (4 tests; all red before).

## Regression Strategy

`workspace/tests`, agent rules check, context budget.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Effective on merge.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
