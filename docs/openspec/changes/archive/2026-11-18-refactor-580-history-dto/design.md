> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

`HistoryMapper.toDto` returns `DtoHistorySummary`. `History.getDto` returned `DtoHistory` and called `DeedManagement.getDto`. A repository search found no caller of that method.

## Goals / Non-Goals

**Goals:**

- The history entity imports no transport DTO.
- The summary mapper is unchanged.

**Non-Goals:**

- Changing history HTTP responses.

## Decisions

Delete the method. Do not add a second mapper for an unused `DtoHistory` shape.

## Riesgos / Trade-offs

- [A reflective caller would break] → the search covered `backend-api/src` and found only the method itself.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| The entity has no getDto | unit | `HistoryDtoRemovalTest` |
| Summary mapping is unchanged | unit | `HistoryMapperTest` |
| The ratchet shrinks | unit | `EntityDtoMappingRatchetTest` |

TDD: the new test failed because `getDto` was still declared.

## Regression Strategy

- Those three tests.
- `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no
- Smoke: the unit test

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
