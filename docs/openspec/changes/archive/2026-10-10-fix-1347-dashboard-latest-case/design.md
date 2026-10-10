# Design

## Context

The trace endpoint answers 400 when the management has no procedure, no procedure type or no workflow definition, and the management summary carries none of that, so the client must ask for traces. On the dev DB runs of about 10 consecutive new managements have no workflow.

## Goals / Non-Goals

Goal: the newest traceable case, with an honest empty state. Non-goals: a backend `hasWorkflow` filter (possible follow-up if the probing cost matters).

## Decisions

Newest = highest idManagement, the same order as the managements list (#1393). Batches of 5 parallel trace calls: typically 1 or 2 batches, at most 4 (20 traces) before the empty state. 'View all' is removed rather than linked, because the grid it heads already shows every module.

## Riesgos / Trade-offs

Each probed management without a workflow logs one 400 in the browser logger and the backend log.

## Testing Strategy

`dashboard-hero.test.tsx` (failing first: module missing) and TS-0035 (2 failing first: the hero showed the oldest case and the button existed).

## Regression Strategy

`bash frontend/verify.sh`; Playwright TS-0035 and the chromium suite against the branch build.

## Playwright Strategy

TS-0035: a case created with a self-seeded workflow (or a newer one) is shown, never the oldest; the modules header has no 'view all' button.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
