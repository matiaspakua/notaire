> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1941 closed; people/budgets/deeds/managements already have list errors + required fields. Most other pages do not.

## Goals / Non-Goals

**Goals:** Uniform list error UX and form validation on remaining surfaces; docs + partial coverage.
**Non-Goals:** New RF features; MFA; full coverage 80%.

## Decisions

1. Use common.loadError for generic list failures.
2. Partial #1246 ratchet only on form helpers.
3. Do not fold #774 into this PR.

## Riesgos / Trade-offs

- [Many pages touch] → mechanical DataTable wiring + focused form modules.
- [i18n missing keys] → add common.loadError in es/en.

## Testing Strategy

Unit tests for copies/concepts validation; existing required-field-errors tests; Playwright optional for one module.

## Regression Strategy

People/budgets/deeds/managements Wave 5–7 behaviour unchanged.

## Playwright Strategy

Unit/Vitest primary; optional Playwright if time.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
