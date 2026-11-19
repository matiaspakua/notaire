> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Exact-symbol search was about 1.9s against `rg` at milliseconds. Search alone recalled 1/6 of the Folio.getDto caller files. The gate printed adoption 0% with 0 sessions.

## Goals / Non-Goals

**Goals:**

- An identifier the name index already resolved does not embed.
- A caller linked by a `calls` edge is present even when full-text search is empty.
- The gate does not print a percentage it did not measure.

**Non-Goals:**

- Changing prose search.
- Inventing a session id for CLI calls.

## Decisions

Skip the dense query only for a query with no whitespace that has at least one exact name or qualname hit. Caller nodes are added at half the exact-hit weight. Adoption text is `n/a` when `sessions` is 0.

## Riesgos / Trade-offs

- [A one-word question that matches a symbol name skips the embedding] → the name hit and full-text search still rank results, and a question with spaces still embeds.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Exact symbol does not embed; prose does | unit | `test_an_exact_symbol_skips_the_embedding_query_and_lists_its_caller` |
| Caller survives an empty full-text result | unit | same test |
| Gate line | unit | `test_gate_line_does_not_report_zero_adoption_when_there_are_no_sessions` |
| Existing adoption percentage | unit | `test_hook_and_report_measure_hit_rate_fallbacks_and_adoption` |

TDD: the exact-symbol test failed with `assert 1 == 0` on the embed count. The gate test failed because the line contained `adoption 0%`.

## Regression Strategy

- The two new tests plus the existing search and adoption tests.
- `bash framework/ai-sdlc/repolens/verify.sh`
- `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no product UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no
- Smoke: `verify.sh` CLI search for `greet`

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
