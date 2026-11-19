> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

The movement table grows with inscriptions. The list endpoint returned every row. The dashboard reads movements from the testimony payload instead.

## Goals / Non-Goals

**Goals:**

- The list endpoint returns a page.
- Existing array readers in tests and Bruno read `content`.

**Non-Goals:**

- Paging the testimony screen.
- A shared rate-limit store.

## Decisions

Default 20, sort `idTestimonyMovement,desc`. Unknown sort properties fall back to that order so a client cannot sort on an unindexed text column by guessing a name.

## Riesgos / Trade-offs

- [Clients that expect a JSON array break] → the UI does not call this path. Bruno and the two tests that asserted an array now read `content`.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Unknown sort falls back | unit | `MovementPageSortTest` |
| Controller accepts pageable | unit | `SimpleControllersTest` |
| Body is a page | integration | `PreviouslyBrokenEndpointsIntegrationTest` |

TDD: `MovementPageSortTest` did not compile until the class existed.

## Regression Strategy

- The tests above.
- `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — the testimony-movements screen does not call this list.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no
- Smoke: the list returns a page

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
