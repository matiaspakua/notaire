> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

After P0.2, CI path scoping is mostly `dorny/paths-filter`, not classic `on.paths`.
The offline metrics heuristic still reported 2/17.

## Goals / Non-Goals

**Goals:** Accurate path-scoping count for Owner evidence; regenerate baseline.

**Non-Goals:** Changing CI workflows; closing #1256 (403).

## Decisions

1. Count a workflow once if it uses `dorny/paths-filter` **or** classic `paths:`.
2. Keep #1445 / Owner decisions untouched; this PR Refs #1256 only.

## Riesgos / Trade-offs

- [Heuristic drift] → unit test pins ≥6 and notes mention dorny.

## Testing Strategy

| AC | Test |
|----|------|
| dorny workflows counted | `test_paths_filter_count_includes_dorny_action` |
| baseline regenerated | committed markdown shows count 6 |

## Regression Strategy

- Existing offline JSON/markdown tests remain.

## Observability / Security / Data

- None.

## Playwright Strategy

- n/a

## Deployment Strategy

- Docs + workspace script only.

## Rollback Strategy

- Revert PR.
