> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Packaging OpenSpec changes for Pages business docs and Owner-tracker retargets are merged on main.

## Goals / Non-Goals

**Goals:** Archive completed trees; link business README to Pages.

**Non-Goals:** Owner decisions; reopening closed umbrellas.

## Decisions

1. Archive with `--skip-specs` (skip_specs changes).
2. Keep `docs-1197-repository-topology` active until Owner decides ADR-024.

## Riesgos / Trade-offs

- [Incomplete task checkboxes in archived trees] → `--yes` continues; historical record preserved.

## Testing Strategy

| AC | Test |
|----|------|
| Archive paths present + README link | `workspace/tests/test_docs_business_pages_link.py` |

## Regression Strategy

- Existing pages business / adr024 guards stay green.

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a

## Deployment Strategy

- Docs-only; Pages already serves `/docs/business/`.

## Rollback Strategy

- Revert PR; restore OpenSpec trees from git history if needed.
