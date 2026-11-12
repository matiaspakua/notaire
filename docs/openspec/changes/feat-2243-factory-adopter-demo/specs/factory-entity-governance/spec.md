<!-- Delta: Phase E allows adopter profiles to ship an approved demo workflow. -->

# factory-entity-governance Specification (delta)

## MODIFIED Requirements

### Requirement: Workflow governance state machine

Workflows SHALL advance `draft → defined → validated → approved` through guarded
transitions. Adopter profiles MAY ship an `approved` workflow for factory UI
demos when pins resolve and approve guards pass (Owner override when
approvals_live=false). List/show SHALL never execute workflow steps.

#### Scenario: happy path reaches approved with owner override when not live

- **WHEN** a workflow with resolvable refs is defined, validated with a waive
  reason, and approved with `--owner-override --reason` while approvals_live=false
- **THEN** governance becomes `approved` and list `--approved` includes it
