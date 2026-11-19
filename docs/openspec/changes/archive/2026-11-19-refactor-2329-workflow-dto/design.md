> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

The three workflow entities each exposed `toDto`. Only the trace service called the node and transition methods. The definition method had no production caller. The web controllers already copy the fields themselves.

## Goals / Non-Goals

**Goals:**

- The three entities import no transport DTO.
- The trace service still copies the same node and transition fields.

**Non-Goals:**

- Changing the controller mappers.

## Decisions

One mapper class holds the three copies, next to the trace service.

## Riesgos / Trade-offs

- [The controllers keep a second copy of the fields] → this slice does not change HTTP responses.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Definition fields | unit | `WorkflowLegacyMapperTest` |
| Node links and null links | unit | `WorkflowLegacyMapperTest` |
| Transition endpoints | unit | `WorkflowLegacyMapperTest` |
| No entity toDto | unit | `WorkflowLegacyMapperTest` |

TDD: the test did not compile until the mapper existed.

## Regression Strategy

- Mapper and ratchet tests.
- `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no
- Smoke: the mapper unit test

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
