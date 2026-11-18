> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

`_scope` returned `same_dir[:3]` before the strict unique-in-project rule. `getDto` is common in `business/`, so impact listed the wrong callers.

## Goals / Non-Goals

**Goals:**

- An ambiguous strict call in one directory links to no method of that name.
- A unique method name in that directory still links.

**Non-Goals:**

- Type-aware resolution of `deed.getDto()`.

## Decisions

Fall through to the existing strict rule when the same-directory bucket has more than one candidate. That rule already requires a single project-wide candidate and a name of at least six characters.

## Riesgos / Trade-offs

- [True callers of an ambiguous name are no longer inferred] → impact stops claiming them. The fixture records both sides.
- [The live Notaire index is unchanged until the next index] → this patch changes linking on the next index, not the store already on disk.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| `Reader.show` does not link either `getDto`; `Reader.once` links `onlyOnce` | unit | `test_an_ambiguous_same_directory_call_is_not_guessed` |

TDD: the first version of the test failed because `Reader.show` linked `Folio.getDto`.

## Regression Strategy

- `bash framework/ai-sdlc/repolens/verify.sh`
- `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no product UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no
- Smoke: `verify.sh`

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
