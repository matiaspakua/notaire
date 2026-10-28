# ui-workflow-chrome Specification

## Purpose

Align workflow viewer and dashboard hero empty/loading/error chrome with the shared design system.

## Requirements

### Requirement: WorkflowViewer empty uses EmptyState

When a WorkflowViewer has no nodes it SHALL render the shared EmptyState with the
translated empty message.

#### Scenario: Viewer empty uses EmptyState

- **GIVEN** a WorkflowViewer with zero nodes
- **WHEN** it renders
- **THEN** an EmptyState is visible with the empty message

### Requirement: WorkflowHero empty uses EmptyState with next action

When the dashboard WorkflowHero has no recent traced management it SHALL render
EmptyState including a link to the managements list.

#### Scenario: Hero empty links to managements

- **GIVEN** no recent management with a workflow
- **WHEN** WorkflowHero renders
- **THEN** EmptyState is shown with a link to `/dashboard/managements`

### Requirement: Workflow node type badge is not decorative ALL-CAPS

WorkflowNodeBox type badges SHALL NOT use CSS `text-transform: uppercase`.

#### Scenario: Node badge has no uppercase transform

- **GIVEN** a WorkflowNodeBox
- **WHEN** it renders the type badge
- **THEN** the badge style does not set `textTransform` to `uppercase`
