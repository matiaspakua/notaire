> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

The only production callers are `findById`, `search`, and `putOnHold` on the ops adapter.

## Goals / Non-Goals

**Goals:**

- The folder entity imports no transport DTO.
- The adapter still returns the same fields.

**Non-Goals:**

- Paging the management-scoped folder search.

## Decisions

The mapper sits next to the ops adapter. A missing management or procedure leaves that id null.

## Riesgos / Trade-offs

- [The field list is copied once] → the mapper test covers it, including null links.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Fields copy | unit | `ProcedureFolderLegacyMapperTest` |
| Null links | unit | `ProcedureFolderLegacyMapperTest` |
| No getDto | unit | `ProcedureFolderLegacyMapperTest` |
| Ratchet shrinks | unit | `EntityDtoMappingRatchetTest` |

TDD: the test did not compile until the mapper existed.

## Regression Strategy

- Those tests.
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
