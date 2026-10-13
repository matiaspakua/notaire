> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §10 Rules for AI Agents.

## Context

The GitHub API has no compare-and-set on labels. Comments do have server-assigned creation times, so they give a total order.

## Goals / Non-Goals

**Goals:** one owner per issue; no conflicting lanes; recovery from dead agents.
**Non-Goals:** a lock server; a queue other than the GitHub board.

## Decisions

1. **Marker race on comments.** Every claimant posts, waits `--settle` (15 s), and re-reads. The minimum `(created_at, nonce)` wins, a deterministic tiebreak. Losers delete their marker.
2. **A release marker resets the race**, so old claims don't win forever.
3. **Stale means older than N hours with no open PR.** An open PR shows the agent is alive.
4. **Board write failure is a handoff, not an error**, until #1648.

## Riesgos / Trade-offs

- [Lane check and marker post are not one step] → a second claimant in a *different* conflicting issue can slip in within the settle window. Accepted: the window is seconds, and the lane holder is visible at once. The foreman runs serially per fleet.
- [PR search by issue number in the body is fuzzy] → it errs toward "has PR", which keeps the claim (safe).

## Testing Strategy

`workspace/tests/test_claim_issue.py`: decision functions and three end-to-end flows with a fake client. Live smoke: refusals for catalog #154 and busy-lane #1637, with no write performed.

## Regression Strategy

`workspace/tests`, `test_milestone_policy.py`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Effective on merge for agents that read the updated playbooks.

## Rollback Strategy

Revert the PR. Manual claiming still works.

## Migration Plan

None.

## Open Questions

None.
