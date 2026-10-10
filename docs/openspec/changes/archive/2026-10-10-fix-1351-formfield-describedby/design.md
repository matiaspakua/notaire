# Design

## Context

FormField wraps label text and control in a <label>; error/helper were nested in it.

## Goals / Non-Goals

Goal: errors and helpers reach assistive tech through aria-describedby; backward-compatible API. Non-goals: client-side validation (separate epic item).

## Decisions

Context consumed by Input/SelectTrigger rather than cloneElement for every child, because many FormFields wrap composite children; native controls are cloned. Messages moved outside the <label> so they are not part of the accessible name.

## Riesgos / Trade-offs

Accessible names no longer include helper/error text; locators matched on the label text are unaffected.

## Testing Strategy

form-field.test.tsx and the TS-0015/TS-0095 assertions failed first (test commit 14126904): no aria-describedby, no role=alert, emoji in the text.

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build.

## Playwright Strategy

TS-0015: an empty person submit marks Nombre invalid with an accessible description; TS-0095: the backend field error is role=alert and describes the input.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
