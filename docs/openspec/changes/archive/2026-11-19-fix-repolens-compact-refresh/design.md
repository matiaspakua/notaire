> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

The MCP search tool returned `k` detailed hits (default 10) plus 10 one-line hits, each detailed hit carrying a 160-character signature and every symbol. The CLI query called `index(wait=True)` when `indexed_head` was not HEAD, so the agent paid the incremental index inside the first search.

## Goals / Non-Goals

**Goals:**

- The default text answer is shorter.
- The query process returns without running the index.

**Non-Goals:**

- Changing hybrid ranking.
- A second embedding of Notaire.

## Decisions

Detailed hits default to 4, with 4 one-line extras. The detached refresh is the same `index --no-wait` the git hook starts, with `--retry-busy` so a connection that is still closing does not cancel it. The query answers from the graph it already opened.

## Riesgos / Trade-offs

- [The first answer after a merge can miss a symbol that landed in that merge] → the stderr line says the refresh is in the background. The next query sees it. `git pull` / `git merge` still start the refresh from `post-merge` before the agent asks.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Stale query does not print `refreshed in` and a later query finds the new symbol | unit | `test_a_query_does_not_wait_for_the_refresh_after_head_moves` |
| Default search prints at most 4 detailed hits | unit | `test_default_search_keeps_four_detailed_hits` |

TDD: the refresh test failed while stderr still said `refreshed in 0.01s`.

## Regression Strategy

- RepoLens pytest for the factory and interface tests.
- `bash workspace/sdlc/preflight.sh --full` before push.

## Playwright Strategy

n/a — no product UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
