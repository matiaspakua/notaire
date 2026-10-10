> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

ADR-024 and REPO-SPLIT-PLAN already point at #1445. Pages Architecture only
surfaces ADR-022 → #1438; ADR-024 title omits the umbrella.

## Goals / Non-Goals

**Goals:** Name #1445 on Pages Architecture next to ADR-024; pin with unit guard;
archive completed OpenSpec hygiene change from #1449.

**Non-Goals:** Recording Owner decisions; deleting `deprecated/`; LICENSE file.

## Decisions

1. Mirror ADR-022 Pages pattern: put the issue number in the ADR-024 display title.
2. PR/commit footers use only `Refs` + full issue URL (never close/fix/resolve near `#N`).

## Riesgos / Trade-offs

- [Keyword auto-close] → avoid close/fix/resolve tokens adjacent to issue numbers.

## Testing Strategy

| AC | Test |
|----|------|
| Pages Architecture cites #1445 | `test_adr024_owner_tracker.py` |
| validate-sdlc-plan green after archive | `validate-sdlc-plan.sh` |

## Regression Strategy

- Existing ADR-022 / business Pages guards stay green.

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a (static docs site copy; covered by unit guard + Pages deploy on main).

## Deployment Strategy

- Merge docs/Pages PR; GitHub Pages redeploy from main.

## Rollback Strategy

- Revert PR. #1445 remains the human tracker.
