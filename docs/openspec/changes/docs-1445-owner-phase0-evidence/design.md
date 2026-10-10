> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Phase 0 is done on main; Owner pack did not yet show P0.1–P0.5 Done or the 6/17 baseline.

## Goals / Non-Goals

**Goals:** Surface evidence for Owner; keep #1445 open.

**Non-Goals:** Recording ADR-024 Accepted; choosing #1438 A/B/C; adding LICENSE.

## Decisions

1. Pin strings with unit tests (TDD).
2. Refs full URL only for #1445.

## Riesgos / Trade-offs

- [Keyword auto-close] → no close/fix/resolve next to `#1445`.

## Testing Strategy

| AC | Test |
|----|------|
| Phase 0 + metrics on Pages | `test_pages_architecture_surfaces_phase0_done_and_metrics` |
| ADR-024 6/17 + dorny | `test_adr024_records_current_path_scoping_baseline` |

## Regression Strategy

- Existing Owner pack / umbrella tests remain.

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a (static docs copy; unit guards sufficient)

## Deployment Strategy

- Docs + github-page; wait for Pages Deploy after merge.

## Rollback Strategy

- Revert PR.
