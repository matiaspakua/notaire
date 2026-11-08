> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#1982 mislabels E2E as frontendTestClasses.

## Goals / Non-Goals

**Goals:** ['Split Vitest vs Playwright counts', 'Refresh defaults + committed metrics']
**Non-Goals:** ['Recomputing coverage.e2eTests percentage']

## Decisions

['Add e2eSpecFiles field; keep frontendTestClasses name for Vitest for API stability of the field name meaning']

## Riesgos / Trade-offs

['Committed metrics.json jumps when regenerated; charts that assumed frontendTestClasses meant E2E will show Vitest until UI update ships in same PR']

## Testing Strategy

['Vitest asserts loadMetrics merges e2eSpecFiles', 'Regenerate metrics counts locally']

## Regression Strategy

['Existing pages playwright + axe']

## Playwright Strategy

n/a for product app; pages axe already covers site.

## Deployment Strategy

Next Pages deploy on main.

## Rollback Strategy

Revert PR.

## Migration Plan

Old metrics.json without e2eSpecFiles merges DEFAULTS.

## Open Questions

None.
