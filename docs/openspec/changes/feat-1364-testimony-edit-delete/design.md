# Design

## Context

Movements are recorded by the enter-registration, register-inscription, withdraw and re-enter actions. `GET /testimonies` already returns each testimony with its movements.

## Goals / Non-Goals

Goals: correct a testimony's number and notes; delete a mistaken one; show the movement history. Non-goals: manual movement CRUD in the UI (Owner: API-only), editing the deed link.

## Decisions

Read the history from the testimony list instead of loading the whole movement table and filtering it. Refuse the delete (409) instead of cascading, so history survives; the user sees why. Dialog instead of a side sheet: the project has no Sheet component and the history is short.

## Riesgos / Trade-offs

A client that relied on DELETE cascading the movements now gets 409 and must remove them first (API-only). Bruno and E2E teardowns were checked: they remove movements first.

## Testing Strategy

Written first: JUnit (1 of 3 red: 204 instead of 409), Vitest (6 of 6 red).

## Regression Strategy

`bash backend-api/verify.sh`, `bash frontend/verify.sh`, Bruno testimonies + testimony-movements, TS-0012/0031/0032 on chromium.

## Playwright Strategy

TS-0143: edit and delete (golden), withdrawn testimony shows its movement and can't be deleted (edge), 390px usability (mobile).

## Deployment Strategy

Backend + frontend; no migration. Rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
