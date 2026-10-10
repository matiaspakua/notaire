# Design

## Context

Playwright's row accessible name is the concatenated cell text, so `new RegExp(String(n))` matches any row containing those digits.

## Goals / Non-Goals

Goal: the three flaky specs. Non-goals: the same pattern in 9 other specs (later).

## Decisions

Filter rows by an exact cell (`getByRole('cell', { name, exact: true })`) in one shared helper; give the template test's procedure type a unique name.

## Riesgos / Trade-offs

A cell whose text is not exactly the number (formatted values) would not match; the migrated cells show the raw number.

## Testing Strategy

The three specs were observed failing in the full runs for #1392/#1394 (rotating tests) and pass 25/25 after the change.

## Regression Strategy

Playwright TS-0012, TS-0081 and presupuesto-plantilla against a production build.

## Playwright Strategy

TS-0012, TS-0081, presupuesto-plantilla.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
