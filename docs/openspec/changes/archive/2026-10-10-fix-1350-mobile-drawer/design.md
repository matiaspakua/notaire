# Design

## Context

The drawer was a translated <aside> with a click-to-close backdrop.

## Goals / Non-Goals

Goal: modal navigation sheet semantics. Non-goals: the sidebar IA regrouping (#1367), slide motion tokens.

## Decisions

Reuse Radix Dialog primitives directly instead of a new Sheet component: it already provides focus trap, Escape, scroll lock and focus return.

## Riesgos / Trade-offs

Two copies of the nav exist in the DOM only while the sheet is open; the desktop aside drops its test id then so `sidebar` stays unique.

## Testing Strategy

TS-0041 cases failed first: no aria-expanded, focus stayed on the toggle, Escape did not close.

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build.

## Playwright Strategy

TS-0041: aria-expanded/controls, focus on first link, 30 Tabs stay inside, close button, body overflow hidden, Escape returns focus.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
