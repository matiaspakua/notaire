# Workflow tracker: a named group of step buttons, and motion that stops

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1353 |
| Use Case | RF-23 – Saber el estado de un trámite; CU83; CU76 |
| Branch | `fix/1353_workflow_tracker_a11y` |
| Gate 1 status | draft |

## Objetivo

The dashboard workflow graph was an <svg role="img"> whose nodes are focusable buttons (axe nested-interactive, serious): an img has no children in the accessibility tree, so screen readers could not reach the steps while keyboard users tabbed into them. The active edge dot and the current-step pulse also looped forever, which WCAG 2.2.2 does not allow without a pause control.

## What Changes

- `WorkflowTracker`: the svg is `role="group"` named by the workflow name with a translated `aria-roledescription` (`dashboard.workflow.diagramRole`: 'diagrama de flujo' / 'flow diagram'); the node buttons (each opens the detail modal) keep their 'name — state' labels.
- The active-edge dot runs 2 × 2.4s and the in-progress pulse 3 × 1.6s (4.8s each), then stop; reduced-motion users still get neither.
- Vitest `workflow-tracker-a11y.test.tsx`; Playwright TS-0035 #1353 cases; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Interactive diagram content stays in the accessibility tree | #1353, WCAG 4.1.2 | Made explicit |
| Automatic motion stops within 5 seconds | #1353, WCAG 2.2.2 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-workflow-tracker`: Dashboard workflow graph accessibility and motion.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | WorkflowTracker, messages/*.json |
| `testing` | yes | Playwright TS-0035 |

### Surface area

- /dashboard workflow hero

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
