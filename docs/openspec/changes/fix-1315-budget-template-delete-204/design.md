# Design

## Context

PR #1332 aligned 17 DELETE handlers on 204 and left this one at 200 as an open question; the Owner answered: same rule, switch to 204.

## Goals / Non-Goals

Goal: every DELETE answers 204. Non-goals: other status codes.

## Decisions

Follow-up PR rather than a push to #1332, which was merged before the change was ready.

## Riesgos / Trade-offs

An external client checking for exactly 200 on this delete now sees 204; the shipped frontend only checks res.ok.

## Testing Strategy

The new guard and the updated integration test written first and observed failing (documents 200; expected 204 but was 200).

## Regression Strategy

Full backend suite; full Bruno run against this backend (329/329 requests, 558 tests); Playwright chromium presupuesto-plantilla and TS-0024 (28 passed).

## Playwright Strategy

presupuesto-plantilla, TS-0024: 28 passed.

## Deployment Strategy

Backend only.

## Rollback Strategy

Revert the merge commit.
