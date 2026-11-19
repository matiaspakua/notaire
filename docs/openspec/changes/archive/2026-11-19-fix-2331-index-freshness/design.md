> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

`Testimony.getDto` search returned no results because the index was one merge behind. Git hooks refresh after checkout and merge only when they are installed. Cloud merges do not depend on them.

## Goals / Non-Goals

**Goals:**

- A read query whose index commit is not HEAD reindexes incrementally before it answers.
- The stderr line names both commits and the refresh time.

**Non-Goals:**

- Blocking on a refresh when another process holds the index lock.
- Measuring a full Notaire reindex.

## Decisions

The recorded commit is `indexed_head` in the store meta, stamped at the end of every successful index. The CLI waits for the incremental index. The MCP server forces its existing background refresh when the same check is stale, and still answers from the published graph until that run finishes.

## Riesgos / Trade-offs

- [The first query after a merge waits for the incremental index] → the fixture wait was 0.01s. A large diff will wait longer, and the stderr line says so.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| A symbol committed after the index is missed, then found | unit | `test_a_query_refreshes_when_head_moved_past_the_index` |
| The second query does not warn | unit | same test |

TDD: the test failed with `no results for 'brand_new_symbol'` and an empty stderr.

## Regression Strategy

- The new test plus the existing refresh and CLI usage tests.
- `bash framework/ai-sdlc/repolens/verify.sh`
- `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no product UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no
- Smoke: `verify.sh` search for `greet`

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
