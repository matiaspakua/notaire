> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

FORMS-AUDIT-PLAN Phase 1 was source-only. Shared contract lives in form-patterns.tsx, form-field-context.tsx, input.tsx, select.tsx. People page already uses FormField, CheckboxField, aria-invalid, and empty next action (#2071). This change proves the rendered desk and locks gaps.

## Goals / Non-Goals

**Goals:** Rendered Impeccable audit evidence (desktop + 320px); contract compliance for people dialog; TDD locks; update FORMS-AUDIT-PLAN.
**Non-Goals:** Admin catalogs (#2088); GitHub Pages; #2070–#2074; restyling unrelated modules.

## Decisions

Prefer unit + one Playwright extension over a large E2E rewrite. Harden only verified gaps (e.g. CheckboxField target size, busy/disabled honesty). Keep people page structure; do not invent a new form system.

## Riesgos / Trade-offs

[False green from source-only checks] → Record rendered audit engine and viewports. [Scope creep into admin catalogs] → Explicit OOS #2088.

## Testing Strategy

Unit: people-form-rendered-a11y.test.tsx (+ form-patterns CheckboxField target if changed). Playwright: extend TS-0156 or add TS-people form a11y for touch height and focus order.

## Regression Strategy

Existing people-identification-type, people-empty-next-action, form-field, TS-0156 must stay green.

## Playwright Strategy

Extend people form Playwright coverage for primary action height at 320px and keyboard focus into the dialog fields.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
