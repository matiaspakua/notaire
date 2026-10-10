# ui-workflow-tracker — delta

## Purpose

Dashboard workflow graph accessibility and motion.

## MODIFIED Requirements

### Requirement: Workflow tracker accessibility

The workflow tracker SHALL expose the diagram as a named group whose steps are buttons named with their state, and its automatic animations SHALL stop within 5 seconds.

#### Scenario: Steps reachable

- **WHEN** a trace with three nodes
- **THEN** the group contains three buttons named 'step — state'

#### Scenario: Bounded motion

- **WHEN** a node is in progress
- **THEN** the edge dot and the pulse stop after 4.8 seconds

#### Scenario: Dashboard

- **WHEN** an admin opens /dashboard
- **THEN** the tracker svg is a group with a role description and no indefinite animation
