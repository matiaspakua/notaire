# Design

## Context

The tracker is a hand-drawn SVG DAG with motion/react animations.

## Goals / Non-Goals

Goal: steps reachable with their state, no endless motion. Non-goals: the ReactFlow workflow editor.

## Decisions

Every node has an action (it opens the detail modal), so the nodes stay role=button inside a named group instead of becoming list items; bounded repeats instead of a pause toggle keep the legend unchanged.

## Riesgos / Trade-offs

The motion now stops after about 5s; the in-progress state is still shown by colour, border and label.

## Testing Strategy

workflow-tracker-a11y.test.tsx (4/4) and the TS-0035 #1353 cases failed first (test commit).

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build.

## Playwright Strategy

TS-0035 #1353: the svg is a group with role description and one state-named button per node; no indefinite animateMotion.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
