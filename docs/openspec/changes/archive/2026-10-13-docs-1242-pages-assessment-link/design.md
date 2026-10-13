> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Assessment and documentation-audit reports shipped; Pages Architecture did not link them.

## Goals / Non-Goals

**Goals:** Discoverability on Architecture Pages.

**Non-Goals:** New assessment content; Owner ADR-024 answers.

## Decisions

1. Place links under Architecture next to Owner pack (enterprise readiness).
2. Pin with unit test (TDD).

## Riesgos / Trade-offs

- None material.

## Testing Strategy

| AC | Test |
|----|------|
| Both report filenames on Architecture page | unit test |

## Regression Strategy

- Existing Owner pack tests remain.

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a

## Deployment Strategy

- github-page Deploy after merge.

## Rollback Strategy

- Revert PR.
